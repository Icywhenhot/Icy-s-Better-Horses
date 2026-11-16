package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CoastalScout implements BreedAbility {

    private static final String KEY = "coastal_scout";
    private static final int TREASURE_RADIUS = 32;
    private final Set<String> found = new LinkedHashSet<>();
    private @Nullable BlockPos treasure;
    private @Nullable BlockPos surface;
    private @Nullable String dimension;
    private int holding;
    private boolean arrived;
    private double water = Double.NaN;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        double bonus = BhAbility.ASINARA_WATER.on(data) ? 0.5D : 0.0D;
        if (bonus != water) {
            water = bonus;
            BhHorseAttributes.apply(horse, ForgeMod.SWIM_SPEED.get(),
                    BhHorseAttributes.Source.ABILITY, KEY, bonus, AttributeModifier.Operation.ADDITION);
        }
        if (!(horse.level() instanceof ServerLevel level)) {
            return;
        }
        if (horse.isBaby() || !data.bh_isOwned() || BhAbility.ASINARA_TREASURE.tier(data) < 2) {
            clearTarget();
            return;
        }
        if (treasure != null) {
            if (!level.dimension().location().toString().equals(dimension)
                    || !level.hasChunkAt(treasure) || !unopened(level, treasure)
                    || horse.distanceToSqr(treasure.getX() + 0.5D, horse.getY(), treasure.getZ() + 0.5D) > 4096.0D
                    || ++holding > 1200) {
                clearTarget();
            }
            return;
        }
        if (horse.tickCount % 100 != 0) {
            return;
        }
        Player owner = level.getPlayerByUUID(data.bh_getOwner());
        if (owner == null || horse.distanceToSqr(owner) > 2304.0D) {
            return;
        }
        BlockPos pos = findTreasure(level, horse);
        if (pos == null) {
            return;
        }
        treasure = pos;
        surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        dimension = level.dimension().location().toString();
        found.add(dimension + "/" + pos.asLong());
        if (found.size() > 128) {
            found.remove(found.iterator().next());
        }
        horse.playSound(SoundEvents.DONKEY_AMBIENT, 1.0F, 1.0F);
        BhSurge.pulse(data, 0, 2);
    }

    private @Nullable BlockPos findTreasure(ServerLevel level, AbstractHorse horse) {
        int minX = (horse.blockPosition().getX() - TREASURE_RADIUS) >> 4;
        int maxX = (horse.blockPosition().getX() + TREASURE_RADIUS) >> 4;
        int minZ = (horse.blockPosition().getZ() - TREASURE_RADIUS) >> 4;
        int maxZ = (horse.blockPosition().getZ() + TREASURE_RADIUS) >> 4;
        double nearest = TREASURE_RADIUS * TREASURE_RADIUS;
        BlockPos best = null;
        String dim = level.dimension().location().toString();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) {
                    continue;
                }
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    double dist = horse.distanceToSqr(pos.getX() + 0.5D, horse.getY(), pos.getZ() + 0.5D);
                    if (dist >= nearest || found.contains(dim + "/" + pos.asLong()) || !unopened(level, pos)) {
                        continue;
                    }
                    nearest = dist;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    private static boolean unopened(ServerLevel level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ChestBlockEntity chest
                && BuiltInLootTables.BURIED_TREASURE.equals(((icy.betterhorses.net.mixin.LootChestAccessor) chest).bh_getLootTable());
    }

    private void clearTarget() {
        treasure = null;
        surface = null;
        dimension = null;
        holding = 0;
        arrived = false;
    }

    public static double fishingWait(Player player) {
        if (!BhAbility.ASINARA_FISH.on() && !BhAbility.ASINARA_BITE.on()) {
            return 1.0D;
        }
        double wait = 1.0D;
        for (BhBreedHorse horse : player.level().getEntitiesOfClass(BhBreedHorse.class,
                player.getBoundingBox().inflate(12.0D))) {
            IHorseData data = IHorseData.of(horse);
            if (!horse.isAlive() || horse.isBaby() || horse.distanceToSqr(player) > 144.0D || !data.bh_isOwned()) {
                continue;
            }
            if (BhAbility.ASINARA_BITE.tier(data, BhContent.ASINARA_DONKEY.getKey()) >= 2) {
                return 1.0D / 1.3D;
            }
            if (BhAbility.ASINARA_FISH.tier(data, BhContent.ASINARA_DONKEY.getKey()) >= 1) {
                wait = 0.85D;
            }
        }
        return wait;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        water = Double.NaN;
        clearTarget();
        BhHorseAttributes.clear(horse, ForgeMod.SWIM_SPEED.get(),
                BhHorseAttributes.Source.ABILITY, KEY);
    }

    @Override
    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String pos : found) {
            list.add(StringTag.valueOf(pos));
        }
        tag.put("Found", list);
    }

    @Override
    public void load(CompoundTag tag) {
        found.clear();
        ListTag list = tag.getList("Found", Tag.TAG_STRING);
        for (int i = Math.max(0, list.size() - 128); i < list.size(); i++) {
            found.add(list.getString(i));
        }
        clearTarget();
    }

    public static final class TreasureGoal extends Goal {

        private final AbstractHorse horse;
        private @Nullable CoastalScout scout;

        public TreasureGoal(AbstractHorse horse) {
            this.horse = horse;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!(horse instanceof IHorseAbilityHost host)) {
                return false;
            }
            for (BreedAbility ability : host.bh_allAbilities()) {
                if (ability instanceof CoastalScout coastal && coastal.surface != null) {
                    scout = coastal;
                    return eligible();
                }
            }
            return false;
        }

        private boolean eligible() {
            if (horse.isVehicle() || !horse.isAlive()) {
                return false;
            }
            IHorseData data = IHorseData.of(horse);
            Player owner = data.bh_getOwner() == null ? null : horse.level().getPlayerByUUID(data.bh_getOwner());
            return data.bh_getCombatTarget() == null && BhAbility.ASINARA_TREASURE.tier(data) >= 2
                    && owner != null && horse.distanceToSqr(owner) <= 2304.0D
                    && (data.bh_getCommand().equals(BhContent.COMMAND_FOLLOW.getKey())
                    || data.bh_getCommand().equals(BhContent.COMMAND_WANDER.getKey()));
        }

        @Override
        public boolean canContinueToUse() {
            return scout != null && scout.surface != null && eligible();
        }

        @Override
        public void tick() {
            if (scout == null || scout.surface == null) {
                return;
            }
            BlockPos pos = scout.surface;
            horse.getLookControl().setLookAt(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            double dist = horse.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            if (dist < 0.36D) {
                horse.getNavigation().stop();
                if (!scout.arrived) {
                    scout.arrived = true;
                    horse.playSound(SoundEvents.DONKEY_AMBIENT, 1.0F, 1.0F);
                }
            } else if (horse.tickCount % 10 == 0) {
                horse.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 1.0D);
            }
        }

        @Override
        public void stop() {
            horse.getNavigation().stop();
            scout = null;
        }
    }
}
