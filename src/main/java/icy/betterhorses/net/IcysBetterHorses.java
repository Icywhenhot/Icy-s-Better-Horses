package icy.betterhorses.net;

import icy.betterhorses.net.network.OpenRadialPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Mod(IcysBetterHorses.MOD_ID)
public final class IcysBetterHorses {

    public static final String MOD_ID = "icys_better_horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;
    private static final int WILD_HORSE_REPOP_INTERVAL_TICKS = 10 * 20;
    private static final int WILD_HORSE_SEARCH_RADIUS = 64;
    private static final int WILD_HORSE_NEARBY_RADIUS = 64;
    private static final int WILD_HORSE_GROUP_ATTEMPTS = 24;
    private static final int WILD_HORSE_GROUP_MIN = 1;
    private static final int WILD_HORSE_GROUP_MAX = 3;

    public IcysBetterHorses(IEventBus modEventBus) {
        BhConfig.load();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModSounds.register(modEventBus);
        ModAttachments.register(modEventBus);
        modEventBus.addListener(BhNetworking::register);
        modEventBus.addListener(this::registerSpawnPlacements);
        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                EntityType.HORSE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BhHorseSpawnRules::checkHorseSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
            HorseTracker.register(horse);
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            HorseTracker.unregister(horse);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % PASSIVE_BOND_INTERVAL_TICKS == 0) {
            growHorseBond(server);
        }
        if (server.getTickCount() % WILD_HORSE_REPOP_INTERVAL_TICKS == 0) {
            tryRepopulateWildHorses(server);
        }
    }

    public static void handleOpenRadialRequest(ServerPlayer player, int horseId) {
        LOGGER.info("[RADIAL][3a] handleOpenRadialRequest on main thread: player={}, horseId={}",
                player.getName().getString(), horseId);
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            LOGGER.info("[RADIAL][3z] Aborting: findCommandHorse returned null");
            return;
        }

        LOGGER.info("[RADIAL][4] Validation passed, sending OpenRadialPayload(horseId={}) back to player {}",
                horse.getId(), player.getName().getString());
        HorseTracker.armInteractSuppression(player.getUUID(), horse.getId());
        PacketDistributor.sendToPlayer(player, new OpenRadialPayload(horse.getId()));
    }

    public static void handleRadialCommand(ServerPlayer player, int horseId, HorseCommand command) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            return;
        }

        IHorseData data = (IHorseData) horse;
        if (command == HorseCommand.SET_HOME) {
            data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(HorseCommand.STAY);
            player.sendSystemMessage(Component.translatable("message.icys_better_horses.home_set"));
            return;
        }

        if (command == HorseCommand.WANDER) {
            data.bh_setWanderCommand(horse.blockPosition());
            return;
        }
        data.bh_setCommand(command);
    }

    public static void handleCallHorse(ServerPlayer player) {
        if (!(player.getVehicle() instanceof AbstractHorse)) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.CALL_WHISTLE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }

        UUID playerId = player.getUUID();
        AbstractHorse horse = findCallableHorse(player, playerId);
        if (horse == null) {
            return;
        }

        IHorseData data = (IHorseData) horse;
        if (data.bh_getBond() <= 0) {
            return;
        }

        BlockPos target = player.blockPosition();
        if (horse.distanceToSqr(player) > 400.0) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            data.bh_setWanderCommand(target);
            return;
        }

        data.bh_setCommand(HorseCommand.FOLLOW);
    }

    private static AbstractHorse findCallableHorse(ServerPlayer player, UUID playerId) {
        AbstractHorse lastRidden = HorseTracker.getLastRidden(playerId);
        if (lastRidden != null
                && playerId.equals(((IHorseData) lastRidden).bh_getOwner())
                && lastRidden.level() == player.level()
                && lastRidden.isAlive()) {
            return lastRidden;
        }

        AbstractHorse nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        for (AbstractHorse candidate : HorseTracker.getAll()) {
            if (!candidate.isAlive() || candidate.level() != player.level()) {
                continue;
            }
            UUID owner = ((IHorseData) candidate).bh_getOwner();
            if (!playerId.equals(owner)) {
                continue;
            }
            double distSq = candidate.distanceToSqr(player);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private static void growHorseBond(MinecraftServer server) {
        for (AbstractHorse horse : HorseTracker.getAll()) {
            IHorseData data = (IHorseData) horse;
            if (data.bh_getBond() >= 100) {
                continue;
            }

            UUID ownerId = data.bh_getOwner();
            if (ownerId == null) {
                continue;
            }

            ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
            if (owner == null || owner.level() != horse.level() || horse.distanceToSqr(owner) >= 100.0) {
                continue;
            }

            data.bh_setBond(data.bh_getBond() + 1);
        }
    }

    private static void tryRepopulateWildHorses(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) {
                continue;
            }
            if (!(player.level() instanceof ServerLevel level) || !level.dimension().equals(Level.OVERWORLD)) {
                continue;
            }

            BlockPos playerPos = player.blockPosition();
            Optional<ResourceKey<Biome>> biomeKey = level.getBiome(playerPos).unwrapKey();
            if (biomeKey.isEmpty() || !BhBiomeSpawns.isExtraHorseBiome(biomeKey.get())) {
                continue;
            }
            if (hasNearbyWildHorse(level, playerPos)) {
                continue;
            }

            spawnWildHorseGroup(level, player, biomeKey.get());
        }
    }

    private static boolean hasNearbyWildHorse(ServerLevel level, BlockPos center) {
        return !level.getEntitiesOfClass(Horse.class, new AABB(center).inflate(WILD_HORSE_NEARBY_RADIUS), horse -> {
            IHorseData data = (IHorseData) horse;
            return !data.bh_isOwned() && !horse.isPersistenceRequired();
        }).isEmpty();
    }

    private static void spawnWildHorseGroup(ServerLevel level, ServerPlayer player, ResourceKey<Biome> targetBiome) {
        int targetCount = WILD_HORSE_GROUP_MIN
                + level.getRandom().nextInt(WILD_HORSE_GROUP_MAX - WILD_HORSE_GROUP_MIN + 1);
        int spawned = 0;
        SpawnGroupData groupData = null;
        int unloadedSkips = 0;
        int biomeMismatchSkips = 0;
        int invalidSurfaceSkips = 0;
        int spawnRuleSkips = 0;
        int obstructionSkips = 0;
        int addFailureSkips = 0;
        String biomeMismatchSample = null;
        String invalidSurfaceSample = null;
        String spawnRuleSample = null;

        for (int attempt = 0; attempt < WILD_HORSE_GROUP_ATTEMPTS && spawned < targetCount; attempt++) {
            int x = player.getBlockX() + level.getRandom().nextInt(WILD_HORSE_SEARCH_RADIUS * 2 + 1) - WILD_HORSE_SEARCH_RADIUS;
            int z = player.getBlockZ() + level.getRandom().nextInt(WILD_HORSE_SEARCH_RADIUS * 2 + 1) - WILD_HORSE_SEARCH_RADIUS;
            BlockPos surface = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, player.getBlockY(), z));
            if (!level.isLoaded(surface)) {
                unloadedSkips++;
                continue;
            }

            Optional<ResourceKey<Biome>> surfaceBiome = level.getBiome(surface).unwrapKey();
            if (surfaceBiome.isEmpty() || !surfaceBiome.get().equals(targetBiome)) {
                biomeMismatchSkips++;
                if (biomeMismatchSample == null) {
                    biomeMismatchSample = surface + " -> " + surfaceBiome
                            .map(key -> key.location().toString())
                            .orElse("<unregistered>");
                }
                continue;
            }
            if (!SpawnPlacements.isSpawnPositionOk(EntityType.HORSE, level, surface)) {
                invalidSurfaceSkips++;
                if (invalidSurfaceSample == null) {
                    invalidSurfaceSample = surface + " below="
                            + BuiltInRegistries.BLOCK.getKey(level.getBlockState(surface.below()).getBlock());
                }
                continue;
            }
            if (!BhHorseSpawnRules.checkHorseSpawnRules(EntityType.HORSE, level, EntitySpawnReason.NATURAL, surface, level.getRandom())) {
                spawnRuleSkips++;
                if (spawnRuleSample == null) {
                    spawnRuleSample = surface
                            + " below=" + BuiltInRegistries.BLOCK.getKey(level.getBlockState(surface.below()).getBlock())
                            + " light=" + level.getRawBrightness(surface, 0);
                }
                continue;
            }

            Horse horse = EntityType.HORSE.create(level, EntitySpawnReason.NATURAL);
            if (horse == null) {
                addFailureSkips++;
                continue;
            }

            horse.snapTo(surface.getX() + 0.5D, surface.getY(), surface.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!horse.checkSpawnObstruction(level)) {
                obstructionSkips++;
                horse.discard();
                continue;
            }

            groupData = horse.finalizeSpawn(level, level.getCurrentDifficultyAt(surface), EntitySpawnReason.NATURAL, groupData);
            if (!level.addFreshEntity(horse)) {
                addFailureSkips++;
                horse.discard();
                continue;
            }
            spawned++;
        }

        if (spawned > 0) {
            LOGGER.info("[HORSE_REPOP] spawned={} biome={} nearPlayer={} playerPos={}",
                    spawned,
                    targetBiome.location(),
                    player.getName().getString(),
                    player.blockPosition());
            return;
        }

        LOGGER.info("[HORSE_REPOP_SKIP] biome={} nearPlayer={} playerPos={} attempts={} unloaded={} biomeMismatch={} invalidSurface={} spawnRules={} obstruction={} addFailed={} mismatchSample={} invalidSample={} ruleSample={}",
                targetBiome.location(),
                player.getName().getString(),
                player.blockPosition(),
                WILD_HORSE_GROUP_ATTEMPTS,
                unloadedSkips,
                biomeMismatchSkips,
                invalidSurfaceSkips,
                spawnRuleSkips,
                obstructionSkips,
                addFailureSkips,
                biomeMismatchSample,
                invalidSurfaceSample,
                spawnRuleSample);
    }

    private static AbstractHorse findCommandHorse(ServerPlayer player, int horseId, double radius) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        if (!(serverLevel.getEntity(horseId) instanceof AbstractHorse horse)) {
            LOGGER.info("[RADIAL][V1] Fail: entity id {} is not an AbstractHorse in player's level (got {})",
                    horseId,
                    serverLevel.getEntity(horseId) == null
                            ? "null"
                            : serverLevel.getEntity(horseId).getClass().getSimpleName());
            return null;
        }
        if (!horse.isTamed()) {
            LOGGER.info("[RADIAL][V2] Fail: horse {} is not tamed", horseId);
            return null;
        }
        double distSq = horse.distanceToSqr(player);
        if (distSq > radius * radius) {
            LOGGER.info("[RADIAL][V3] Fail: horse {} out of range (distSq={}, maxSq={})",
                    horseId, distSq, radius * radius);
            return null;
        }

        UUID owner = ((IHorseData) horse).bh_getOwner();
        if (owner != null && !owner.equals(player.getUUID())) {
            LOGGER.info("[RADIAL][V4] Fail: horse {} is owned by {}, not by caller {}",
                    horseId, owner, player.getUUID());
            return null;
        }

        LOGGER.info("[RADIAL][V5] OK: horse {} passed all validation (tamed={}, distSq={}, owner={})",
                horseId, horse.isTamed(), distSq, owner);
        return horse;
    }
}
