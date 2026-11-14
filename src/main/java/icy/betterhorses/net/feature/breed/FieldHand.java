package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.goal.HorseWanderBoundsGoal;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FieldHand implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.tickCount % 10 != 0) {
            return;
        }
        ServerPlayer owner = farmer(horse, data);
        if (owner == null) {
            return;
        }
        ServerLevel level = (ServerLevel) horse.level();
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        BlockPos centre = horse.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -1, -2), centre.offset(2, 1, 2))) {
            if (tend(level, pos, horse, data, owner, tier)) {
                return;
            }
        }
    }

    private static @Nullable ServerPlayer farmer(AbstractHorse horse, IHorseData data) {
        if (!(horse.level() instanceof ServerLevel level) || horse.isBaby() || data.bh_getOwner() == null
                || !BhAbility.ANDALUSIAN_FARM.on() || data.bh_isAbilityPaused()
                || !data.bh_hasChestGear() || data.bh_hasEnderChestGear()
                || !EventHooks.canEntityGrief(level, horse) || BhHorseTraits.bondTier(data.bh_getBond()) < 1) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(data.bh_getOwner());
    }

    private static boolean job(ServerLevel level, BlockPos pos, IHorseData data, int tier) {
        if (!level.hasChunkAt(pos) || !level.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
            return false;
        }
        BlockState crop = level.getBlockState(pos);
        if (crop.isAir()) {
            return seedSlot(level, pos, data) >= 0;
        }
        return tier >= 2 && crop.getBlock() instanceof CropBlock plant && plant.isMaxAge(crop);
    }

    private static boolean tend(ServerLevel level, BlockPos pos, AbstractHorse horse, IHorseData data,
                                ServerPlayer owner, int tier) {
        if (!job(level, pos, data, tier) || !level.mayInteract(owner, pos)) {
            return false;
        }
        BlockState crop = level.getBlockState(pos);
        SimpleContainer chest = data.bh_getChestContainer();
        if (crop.isAir()) {
            return plant(level, pos, horse, data, chest);
        }
        return harvest(level, pos, crop, (CropBlock) crop.getBlock(), horse, data, chest);
    }

    private static int slots(SimpleContainer chest, IHorseData data) {
        return Math.min(chest.getContainerSize(), data.bh_getChestRows() * 9);
    }

    private static int seedSlot(ServerLevel level, BlockPos pos, IHorseData data) {
        SimpleContainer chest = data.bh_getChestContainer();
        for (int i = 0; i < slots(chest, data); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem item
                    && item.getBlock() instanceof CropBlock crop && crop.getStateForAge(0).canSurvive(level, pos)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean plant(ServerLevel level, BlockPos pos, AbstractHorse horse, IHorseData data, SimpleContainer chest) {
        int slot = seedSlot(level, pos, data);
        if (slot < 0) {
            return false;
        }
        ItemStack stack = chest.getItem(slot);
        CropBlock crop = (CropBlock) ((BlockItem) stack.getItem()).getBlock();
        if (!place(level, pos, crop.getStateForAge(0), horse)) {
            return false;
        }
        stack.shrink(1);
        chest.setChanged();
        return true;
    }

    private static boolean harvest(ServerLevel level, BlockPos pos, BlockState state, CropBlock crop,
                                   AbstractHorse horse, IHorseData data, SimpleContainer chest) {
        if (!EventHooks.onEntityDestroyBlock(horse, pos, state)) {
            return false;
        }
        Item seed = crop.getCloneItemStack(level, pos, state).getItem();
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), horse, ItemStack.EMPTY);
        ItemStack spare = drops.stream().filter(drop -> drop.is(seed)).findFirst().orElse(ItemStack.EMPTY);
        int slot = -1;
        if (spare.isEmpty()) {
            for (int i = 0; i < slots(chest, data); i++) {
                if (chest.getItem(i).is(seed)) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) {
                return false;
            }
        }
        if (!place(level, pos, crop.getStateForAge(0), horse)) {
            return false;
        }
        if (slot >= 0) {
            chest.getItem(slot).shrink(1);
        } else {
            spare.shrink(1);
        }
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack rest = DonkeyForaging.insert(data, drop);
            if (!rest.isEmpty()) {
                Block.popResource(level, pos, rest);
            }
        }
        chest.setChanged();
        return true;
    }

    private static boolean place(ServerLevel level, BlockPos pos, BlockState seed, AbstractHorse horse) {
        BlockState old = level.getBlockState(pos);
        if (!level.setBlock(pos, seed, Block.UPDATE_KNOWN_SHAPE)) {
            return false;
        }
        if (EventHooks.onBlockPlace(horse, BlockSnapshot.create(level.dimension(), level, pos), Direction.UP)) {
            level.setBlock(pos, old, Block.UPDATE_KNOWN_SHAPE);
            return false;
        }
        level.markAndNotifyBlock(pos, level.getChunkAt(pos), old, seed, Block.UPDATE_ALL, 512);
        return true;
    }

    public static void onTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (BhAbility.ANDALUSIAN_FARM.on() && event.getEntity() instanceof BhBreedDonkey donkey
                && donkey.bhFixedBreed().equals(BhContent.ANDALUSIAN_DONKEY.getKey())) {
            event.setCanceled(true);
        }
    }

    public static final class TendGoal extends Goal {

        private static final int SCAN = 10;

        private final AbstractHorse horse;
        private final Set<BlockPos> skipped = new HashSet<>();
        private @Nullable BlockPos target;
        private int nextScan;
        private int forget;
        private int ready;
        private int stuck;

        public TendGoal(AbstractHorse horse) {
            this.horse = horse;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (horse.tickCount < nextScan || !eligible()) {
                return false;
            }
            nextScan = horse.tickCount + 40;
            if (horse.tickCount >= forget) {
                skipped.clear();
                forget = horse.tickCount + 1200;
            }
            target = next();
            return target != null;
        }

        private boolean eligible() {
            if (horse.isVehicle() || horse.isLeashed() || !(horse instanceof IHorseAbilityHost host)
                    || host.bh_allAbilities().stream().noneMatch(ability -> ability instanceof FieldHand)) {
                return false;
            }
            IHorseData data = IHorseData.of(horse);
            return data.bh_getCombatTarget() == null && data.bh_getWanderCenter() != null
                    && data.bh_getCommand().equals(BhContent.COMMAND_WANDER.getKey())
                    && farmer(horse, data) != null;
        }

        private @Nullable BlockPos next() {
            IHorseData data = IHorseData.of(horse);
            ServerPlayer owner = farmer(horse, data);
            BlockPos home = data.bh_getWanderCenter();
            if (owner == null || home == null) {
                return null;
            }
            ServerLevel level = (ServerLevel) horse.level();
            int tier = BhHorseTraits.bondTier(data.bh_getBond());
            BlockPos here = horse.blockPosition();
            BlockPos best = null;
            double closest = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(here.offset(-SCAN, -3, -SCAN), here.offset(SCAN, 3, SCAN))) {
                if (Math.abs(pos.getX() - home.getX()) > HorseWanderBoundsGoal.WANDER_HALF_EXTENT
                        || Math.abs(pos.getZ() - home.getZ()) > HorseWanderBoundsGoal.WANDER_HALF_EXTENT) {
                    continue;
                }
                double dist = pos.distSqr(here);
                if (dist >= closest || skipped.contains(pos) || !job(level, pos, data, tier)
                        || !level.mayInteract(owner, pos)) {
                    continue;
                }
                best = pos.immutable();
                closest = dist;
            }
            return best;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && eligible();
        }

        @Override
        public void start() {
            stuck = 0;
            walk();
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            horse.getLookControl().setLookAt(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D);
            double dx = target.getX() + 0.5D - horse.getX();
            double dz = target.getZ() + 0.5D - horse.getZ();
            if (dx * dx + dz * dz <= 4.0D && Math.abs(target.getY() - horse.getBlockY()) <= 1) {
                if (horse.tickCount < ready) {
                    return;
                }
                ready = horse.tickCount + 4;
                IHorseData data = IHorseData.of(horse);
                ServerPlayer owner = farmer(horse, data);
                ServerLevel level = (ServerLevel) horse.level();
                int tier = BhHorseTraits.bondTier(data.bh_getBond());
                if (owner != null && job(level, target, data, tier)
                        && !tend(level, target, horse, data, owner, tier)) {
                    skipped.add(target);
                }
                target = next();
                stuck = 0;
                walk();
                return;
            }
            if (++stuck > 200) {
                skipped.add(target);
                target = next();
                stuck = 0;
                walk();
                return;
            }
            if (horse.tickCount % 10 == 0 || horse.getNavigation().isDone()) {
                walk();
            }
        }

        private void walk() {
            if (target != null) {
                horse.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, 1.0D);
            }
        }

        @Override
        public void stop() {
            horse.getNavigation().stop();
            target = null;
        }
    }
}
