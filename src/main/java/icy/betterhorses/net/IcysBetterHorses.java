package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.OpenRadialPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.RequestOpenRadialPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class IcysBetterHorses implements ModInitializer {

    public static final String MOD_ID = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ResourceLocation WATER_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "water_speed");
    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;

    private static final int WILD_HORSE_REPOP_INTERVAL_TICKS = 10 * 20;
    private static final int WILD_HORSE_SEARCH_RADIUS = 64;
    private static final int WILD_HORSE_NEARBY_RADIUS = 64;
    private static final int WILD_HORSE_GROUP_ATTEMPTS = 24;
    private static final int WILD_HORSE_GROUP_MIN = 1;
    private static final int WILD_HORSE_GROUP_MAX = 3;

    @Override
    public void onInitialize() {
        BhConfig.load();
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModSounds.init();
        BhBiomeSpawns.register();
        BhHorseSpawnRules.installSpawnPlacementOverride();
        registerPackets();
        registerServerHandlers();
        registerEntityTracking();
        registerTickEvents();
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void registerPackets() {
        PayloadTypeRegistry.playC2S().register(RadialCommandPayload.TYPE, new RadialCommandPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(CallHorsePayload.TYPE, new CallHorsePayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(RequestOpenRadialPayload.TYPE, new RequestOpenRadialPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(OpenRadialPayload.TYPE, new OpenRadialPayload.StreamCodec());
    }

    private void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(RadialCommandPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            HorseCommand command = HorseCommand.fromId(payload.commandOrdinal());
            context.server().execute(() -> handleRadialCommand(player, payload.horseId(), command));
        });

        ServerPlayNetworking.registerGlobalReceiver(CallHorsePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleCallHorse(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestOpenRadialPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            LOGGER.info("[RADIAL][3] C2S received RequestOpenRadialPayload(horseId={}) from player {}",
                    payload.horseId(), player.getName().getString());
            context.server().execute(() -> handleOpenRadialRequest(player, payload.horseId()));
        });
    }

    private void handleOpenRadialRequest(ServerPlayer player, int horseId) {
        LOGGER.info("[RADIAL][3a] handleOpenRadialRequest on main thread: player={}, horseId={}",
                player.getName().getString(), horseId);
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            LOGGER.info("[RADIAL][3z] Aborting: findCommandHorse returned null");
            return;
        }

        LOGGER.info("[RADIAL][4] Validation passed, sending OpenRadialPayload(horseId={}) back to player {}",
                horse.getId(), player.getName().getString());
        // Arm before the vanilla ServerboundInteractPacket arrives in this same tick, so the
        // mount path in AbstractHorse#mobInteract gets short-circuited and the player doesn't
        // end up riding the horse just because Ctrl+rightclick also fires the vanilla interact.
        HorseTracker.armInteractSuppression(player.getUUID(), horse.getId());
        ServerPlayNetworking.send(player, new OpenRadialPayload(horse.getId()));
    }

    private void handleRadialCommand(ServerPlayer player, int horseId, HorseCommand command) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) return;

        IHorseData data = (IHorseData) horse;
        if (command == HorseCommand.SET_HOME) {
            data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(HorseCommand.STAY);
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.home_set"));
        } else {
            data.bh_setCommand(command);
            if (command == HorseCommand.WANDER) {
                data.bh_setWanderCenter(horse.blockPosition());
            }
        }
    }

    private void handleCallHorse(ServerPlayer player) {
        if (!(player.getVehicle() instanceof AbstractHorse)) {
            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.CALL_WHISTLE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F);
        }

        UUID playerId = player.getUUID();
        AbstractHorse horse = findCallableHorse(player, playerId);
        if (horse == null) return;

        IHorseData data = (IHorseData) horse;
        if (data.bh_getBond() <= 0) return;

        // Whistling always cancels whatever standing order the horse was on (STAY,
        // RETURN_HOME, etc.) — the player explicitly wants the horse to come to them.
        data.bh_setCommand(HorseCommand.FOLLOW);

        BlockPos target = player.blockPosition();
        if (horse.distanceToSqr(player) > 400.0) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        }
    }

    /**
     * Resolve which horse the whistle should summon for {@code player}. Prefers the last horse
     * this player rode, falling back to the nearest owned horse in the same level (the last-ridden
     * map is process-static and empty on every server boot).
     */
    private AbstractHorse findCallableHorse(ServerPlayer player, UUID playerId) {
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
            if (!candidate.isAlive() || candidate.level() != player.level()) continue;
            UUID owner = ((IHorseData) candidate).bh_getOwner();
            if (!playerId.equals(owner)) continue;
            double distSq = candidate.distanceToSqr(player);
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private void registerEntityTracking() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
                HorseTracker.register(horse);
            }
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity instanceof AbstractHorse horse) {
                HorseTracker.unregister(horse);
            }
        });
    }

    private void registerTickEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            updateMountedWaterSpeed(server);
            if (server.getTickCount() % PASSIVE_BOND_INTERVAL_TICKS == 0) {
                growHorseBond(server);
            }
            if (server.getTickCount() % WILD_HORSE_REPOP_INTERVAL_TICKS == 0) {
                tryRepopulateWildHorses(server);
            }
        });
    }

    private void updateMountedWaterSpeed(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.getVehicle() instanceof AbstractHorse horse)) continue;

            AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed == null) continue;

            boolean inWater = horse.isInWater();
            boolean hasModifier = speed.getModifier(WATER_SPEED_ID) != null;
            if (inWater == hasModifier) continue;

            if (inWater) {
                speed.addTransientModifier(new AttributeModifier(
                        WATER_SPEED_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else {
                speed.removeModifier(WATER_SPEED_ID);
            }
        }
    }

    private void growHorseBond(net.minecraft.server.MinecraftServer server) {
        for (AbstractHorse horse : HorseTracker.getAll()) {
            IHorseData data = (IHorseData) horse;
            if (data.bh_getBond() >= 100) continue;

            UUID ownerId = data.bh_getOwner();
            if (ownerId == null) continue;

            ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
            if (owner == null || owner.level() != horse.level() || horse.distanceToSqr(owner) >= 100.0) {
                continue;
            }

            data.bh_setBond(data.bh_getBond() + 1);
        }
    }

    private AbstractHorse findCommandHorse(ServerPlayer player, int horseId, double radius) {
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

    private void tryRepopulateWildHorses(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator()) continue;
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

    private boolean hasNearbyWildHorse(ServerLevel level, BlockPos center) {
        return !level.getEntitiesOfClass(Horse.class, new AABB(center).inflate(WILD_HORSE_NEARBY_RADIUS), horse -> {
            IHorseData data = (IHorseData) horse;
            return !data.bh_isOwned() && !horse.isPersistenceRequired();
        }).isEmpty();
    }

    private void spawnWildHorseGroup(ServerLevel level, ServerPlayer player, ResourceKey<Biome> targetBiome) {
        int targetCount = WILD_HORSE_GROUP_MIN
                + level.getRandom().nextInt(WILD_HORSE_GROUP_MAX - WILD_HORSE_GROUP_MIN + 1);
        int spawned = 0;
        SpawnGroupData groupData = null;

        for (int attempt = 0; attempt < WILD_HORSE_GROUP_ATTEMPTS && spawned < targetCount; attempt++) {
            int x = player.getBlockX() + level.getRandom().nextInt(WILD_HORSE_SEARCH_RADIUS * 2 + 1) - WILD_HORSE_SEARCH_RADIUS;
            int z = player.getBlockZ() + level.getRandom().nextInt(WILD_HORSE_SEARCH_RADIUS * 2 + 1) - WILD_HORSE_SEARCH_RADIUS;
            BlockPos surface = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, player.getBlockY(), z));
            if (!level.isLoaded(surface)) {
                continue;
            }

            Optional<ResourceKey<Biome>> surfaceBiome = level.getBiome(surface).unwrapKey();
            if (surfaceBiome.isEmpty() || !surfaceBiome.get().equals(targetBiome)) {
                continue;
            }
            if (!SpawnPlacements.isSpawnPositionOk(EntityType.HORSE, level, surface)) {
                continue;
            }
            if (!BhHorseSpawnRules.checkHorseSpawnRules(EntityType.HORSE, level, EntitySpawnReason.NATURAL, surface, level.getRandom())) {
                continue;
            }

            Horse horse = EntityType.HORSE.create(level, EntitySpawnReason.NATURAL);
            if (horse == null) {
                continue;
            }

            horse.snapTo(surface.getX() + 0.5D, surface.getY(), surface.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!horse.checkSpawnObstruction(level)) {
                horse.discard();
                continue;
            }

            groupData = horse.finalizeSpawn(level, level.getCurrentDifficultyAt(surface), EntitySpawnReason.NATURAL, groupData);
            if (!level.addFreshEntity(horse)) {
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
        }
    }
}
