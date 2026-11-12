package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhUndeadHorse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BhUndeadSpawns {

    private static final int EVERY = 600;
    private static final int TRIES = 8;
    private static final int NEAR = 24;
    private static final int FAR = 48;
    private static final double CROWD = 96.0D;

    private enum Ground { SURFACE, UNDERGROUND, SEABED }

    private record Kind(TagKey<Biome> biomes, List<EntityType<BhUndeadHorse>> sizes, int odds,
                        @Nullable EntityType<? extends Mob> rider, int riderOdds, Ground ground) {}

    private static final Kind SCULK = new Kind(tag("sculk_horse"), ModEntities.SCULKS, 40, EntityTypes.SKELETON, 3, Ground.UNDERGROUND);
    private static final Kind STRAY = new Kind(tag("stray_horse"), ModEntities.STRAYS, 60, EntityTypes.STRAY, 4, Ground.SURFACE);
    private static final Kind HUSK = new Kind(tag("husk_horse"), ModEntities.HUSKS, 30, EntityTypes.HUSK, 4, Ground.SURFACE);
    private static final Kind DROWNED = new Kind(tag("drowned_horse"), ModEntities.DROWNEDS, 50, null, 0, Ground.SEABED);
    private static final Kind SKELETON = new Kind(tag("undead_horse"), ModEntities.SKELETONS, 80, null, 0, Ground.SURFACE);
    private static final Kind ZOMBIE = new Kind(tag("undead_horse"), ModEntities.ZOMBIES, 80, null, 0, Ground.SURFACE);

    private BhUndeadSpawns() {
    }

    private static TagKey<Biome> tag(String name) {
        return TagKey.create(Registries.BIOME,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "spawns/" + name));
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % EVERY != 0) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (!level.isDarkOutside() || !level.getGameRules().get(GameRules.SPAWN_MOBS)) {
                continue;
            }
            for (ServerPlayer player : level.players()) {
                if (!player.isSpectator()) {
                    trySpawn(level, player);
                }
            }
        }
    }

    private static void trySpawn(ServerLevel level, ServerPlayer player) {
        BlockPos at = player.blockPosition();
        RandomSource rand = level.getRandom();
        Kind kind = level.getBiome(at).is(SCULK.biomes) ? SCULK
                : level.getBiome(at).is(STRAY.biomes) ? STRAY
                : level.getBiome(at).is(HUSK.biomes) ? HUSK
                : level.getBiome(at).is(DROWNED.biomes) ? DROWNED
                : level.getBiome(at).is(SKELETON.biomes) ? rand.nextBoolean() ? SKELETON : ZOMBIE
                : null;
        if (kind == null || rand.nextInt(kind.odds) != 0 || crowded(level, player, kind)) {
            return;
        }
        for (int i = 0; i < TRIES; i++) {
            BlockPos pos = pick(level, at, rand, kind);
            if (pos != null && spawn(level, pos, rand, kind)) {
                return;
            }
        }
    }

    private static boolean crowded(ServerLevel level, ServerPlayer player, Kind kind) {
        return !level.getEntitiesOfClass(BhUndeadHorse.class, player.getBoundingBox().inflate(CROWD),
                horse -> kind.sizes.contains(horse.getType())).isEmpty();
    }

    private static @Nullable BlockPos pick(ServerLevel level, BlockPos at, RandomSource rand, Kind kind) {
        int dist = NEAR + rand.nextInt(FAR - NEAR);
        double angle = rand.nextDouble() * Math.PI * 2.0D;
        int x = at.getX() + (int) Math.round(Math.cos(angle) * dist);
        int z = at.getZ() + (int) Math.round(Math.sin(angle) * dist);
        if (!level.hasChunkAt(new BlockPos(x, at.getY(), z))) {
            return null;
        }
        if (kind.ground == Ground.SURFACE) {
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            return standable(level, top, false) && level.getBiome(top).is(kind.biomes) ? top : null;
        }
        if (kind.ground == Ground.SEABED) {
            BlockPos floor = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z), z);
            return standable(level, floor, true) && level.getBiome(floor).is(kind.biomes) ? floor : null;
        }
        for (int y = at.getY() + 10; y >= at.getY() - 10; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (standable(level, pos, false) && level.getBiome(pos).is(kind.biomes)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean standable(ServerLevel level, BlockPos pos, boolean wet) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getFluidState(below).isEmpty()
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && (wet ? level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER)
                        : level.getFluidState(pos).isEmpty())
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos.above(2)).getCollisionShape(level, pos.above(2)).isEmpty();
    }

    private static boolean spawn(ServerLevel level, BlockPos pos, RandomSource rand, Kind kind) {
        BhUndeadHorse horse = kind.sizes.get(rand.nextInt(kind.sizes.size())).create(level, EntitySpawnReason.NATURAL);
        if (horse == null) {
            return false;
        }
        horse.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, rand.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(horse)) {
            horse.discard();
            return false;
        }
        horse.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
        if (kind.rider != null && level.getDifficulty() != Difficulty.PEACEFUL && rand.nextInt(kind.riderOdds) == 0) {
            Mob rider = kind.rider.create(level, EntitySpawnReason.JOCKEY);
            if (rider != null) {
                rider.snapTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), 0.0F);
                rider.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.JOCKEY, null);
                rider.startRiding(horse, true, true);
            }
        }
        level.addFreshEntityWithPassengers(horse);
        return true;
    }
}
