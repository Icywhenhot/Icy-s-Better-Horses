package icy.betterhorses.net;

import icy.betterhorses.net.mixin.SpawnPlacementsAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class BhHorseSpawnRules {

    private BhHorseSpawnRules() {}

    public static void installSpawnPlacementOverride() {
        Object previous = SpawnPlacementsAccessor.bh_getDataByType().remove(EntityTypes.HORSE);
        SpawnPlacementsAccessor.bh_callRegister(
                EntityTypes.HORSE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BhHorseSpawnRules::checkHorseSpawnRules);
        IcysBetterHorses.LOGGER.info("[SPAWN_RULE_PATCH] Replaced HORSE spawn predicate (hadPrevious={})", previous != null);
    }

    public static boolean appliesTo(EntityType<?> type) {
        return AbstractHorse.class.isAssignableFrom(type.getBaseClass());
    }

    public static boolean checkHorseLikeGroundRules(EntityType<?> type,
                                                    LevelAccessor level,
                                                    EntitySpawnReason reason,
                                                    BlockPos pos) {
        return appliesTo(type) && checkHorseGroundRules(level, reason, pos);
    }

    public static boolean checkHorseSpawnRules(EntityType<Horse> type,
                                               ServerLevelAccessor level,
                                               EntitySpawnReason reason,
                                               BlockPos pos,
                                               RandomSource random) {
        return checkHorseGroundRules(level, reason, pos);
    }

    public static boolean checkHorseGroundRules(LevelAccessor level,
                                                EntitySpawnReason reason,
                                                BlockPos pos) {
        if (!EntitySpawnReason.ignoresLightRequirements(reason) && level.getRawBrightness(pos, 0) <= 8) {
            return false;
        }

        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON)
                || below.is(BlockTags.SAND)
                || below.is(BlockTags.DIRT)
                || below.is(BlockTags.TERRACOTTA)
                || below.is(BlockTags.SNOW)
                || below.is(Blocks.GRAVEL)
                || below.is(Blocks.ICE)
                || below.is(Blocks.PACKED_ICE)
                || below.is(Blocks.POWDER_SNOW)
                || below.is(Blocks.SNOW)
                || below.is(Blocks.SNOW_BLOCK)
                || below.is(Blocks.STONE);
    }
}
