package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

public final class FieldHand implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.level() instanceof ServerLevel level) || horse.isBaby() || data.bh_getOwner() == null
                || !BhAbility.ANDALUSIAN_FARM.on() || data.bh_isAbilityPaused()
                || horse.tickCount % 10 != 0 || !data.bh_hasChestGear() || data.bh_hasEnderChestGear()
                || !EventHooks.canEntityGrief(level, horse)) {
            return;
        }
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (tier < 1) {
            return;
        }
        SimpleContainer chest = data.bh_getChestContainer();
        BlockPos centre = horse.blockPosition();
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(data.bh_getOwner());
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-2, -1, -2), centre.offset(2, 1, 2))) {
            if (!level.hasChunkAt(pos) || !level.getBlockState(pos.below()).is(Blocks.FARMLAND)
                    || owner != null && !level.mayInteract(owner, pos)) {
                continue;
            }
            BlockState crop = level.getBlockState(pos);
            if (tier >= 2 && crop.getBlock() instanceof CropBlock plant && plant.isMaxAge(crop)) {
                if (harvest(level, pos, crop, plant, horse, data, chest)) {
                    return;
                }
            } else if (crop.isAir() && plant(level, pos, horse, data, chest)) {
                return;
            }
        }
    }

    private static boolean plant(ServerLevel level, BlockPos pos, AbstractHorse horse, IHorseData data, SimpleContainer chest) {
        for (int i = 0; i < Math.min(chest.getContainerSize(), data.bh_getChestRows() * 9); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem item)
                    || !(item.getBlock() instanceof CropBlock crop)) {
                continue;
            }
            BlockState seed = crop.getStateForAge(0);
            if (!seed.canSurvive(level, pos) || !place(level, pos, seed, horse)) {
                continue;
            }
            stack.shrink(1);
            chest.setChanged();
            return true;
        }
        return false;
    }

    private static boolean harvest(ServerLevel level, BlockPos pos, BlockState state, CropBlock crop,
                                   AbstractHorse horse, IHorseData data, SimpleContainer chest) {
        ItemStack seed = crop.getCloneItemStack(level, pos, state);
        int slot = -1;
        for (int i = 0; i < Math.min(chest.getContainerSize(), data.bh_getChestRows() * 9); i++) {
            if (chest.getItem(i).is(seed.getItem())) {
                slot = i;
                break;
            }
        }
        if (slot < 0 || !EventHooks.onEntityDestroyBlock(horse, pos, state)) {
            return false;
        }
        List<ItemStack> drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), horse, ItemStack.EMPTY);
        if (!place(level, pos, crop.getStateForAge(0), horse)) {
            return false;
        }
        chest.getItem(slot).shrink(1);
        for (ItemStack drop : drops) {
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
}
