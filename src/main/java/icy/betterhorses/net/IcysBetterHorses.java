package icy.betterhorses.net;

import icy.betterhorses.net.network.OpenRadialPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MobSpawnType;
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
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            return;
        }
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
            if (!BhHorseSpawnRules.checkHorseSpawnRules(EntityType.HORSE, level, MobSpawnType.NATURAL, surface, level.getRandom())) {
                continue;
            }

            Horse horse = EntityType.HORSE.create(level);
            if (horse == null) {
                continue;
            }

            horse.moveTo(surface.getX() + 0.5D, surface.getY(), surface.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            if (!horse.checkSpawnObstruction(level)) {
                horse.discard();
                continue;
            }

            groupData = horse.finalizeSpawn(level, level.getCurrentDifficultyAt(surface), MobSpawnType.NATURAL, groupData);
            if (!level.addFreshEntity(horse)) {
                horse.discard();
                continue;
            }
            spawned++;
        }
    }

    private static AbstractHorse findCommandHorse(ServerPlayer player, int horseId, double radius) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        if (!(serverLevel.getEntity(horseId) instanceof AbstractHorse horse) || !horse.isTamed()) {
            return null;
        }
        if (horse.distanceToSqr(player) > radius * radius) {
            return null;
        }

        UUID owner = ((IHorseData) horse).bh_getOwner();
        if (owner != null && !owner.equals(player.getUUID())) {
            return null;
        }
        return horse;
    }
}
