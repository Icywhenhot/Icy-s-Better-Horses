package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IcysBetterHorses implements ModInitializer {

    public static final String MOD_ID = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;
    private static final double CALL_TELEPORT_DIST_SQ = 32.0 * 32.0; // teleport to the player when whistled from beyond 32 blocks

    // Leftover copies of whistle-respawned horses, discarded on the tick after their chunk loads.
    private final List<AbstractHorse> staleHorses = new ArrayList<>();

    @Override
    public void onInitialize() {
        BhConfig.load();
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModSounds.init();
        ModTicketTypes.init();
        BhBiomeSpawns.register();
        BhHorseSpawnRules.installSpawnPlacementOverride();
        registerPackets();
        registerServerHandlers();
        registerEntityTracking();
        registerTickEvents();
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void registerPackets() {
        PayloadTypeRegistry.serverboundPlay().register(RadialCommandPayload.TYPE, new RadialCommandPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(CallHorsePayload.TYPE, new CallHorsePayload.StreamCodec());
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
            if (command == HorseCommand.WANDER) {
                data.bh_setWanderCenter(horse.blockPosition());
            }
            data.bh_setCommand(command);
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
        if (horse == null) {
            LOGGER.info("[whistle] {} whistled: no loaded horse found, trying stored respawn", player.getName().getString());
            respawnStoredHorse(player, playerId);
            return;
        }

        LOGGER.info("[whistle] {} whistled: summoning loaded horse {}", player.getName().getString(), horse.getUUID());
        summonHorseToPlayer(horse, player);
    }

    // Whistling always cancels whatever standing order the horse was on — the player explicitly wants it to come to them.
    private void summonHorseToPlayer(AbstractHorse horse, ServerPlayer player) {
        IHorseData data = (IHorseData) horse;
        if (data.bh_getBond() <= 0) return;

        data.bh_setCommand(HorseCommand.FOLLOW);

        BlockPos target = player.blockPosition();
        if (horse.distanceToSqr(player) > CALL_TELEPORT_DIST_SQ) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        }
    }

    // The last-ridden horse is somewhere in an unloaded chunk. Instead of chasing the entity, spawn a
    // fresh copy from its stored NBT snapshot right at the player, and bump the generation counter so
    // the copy left behind in the unloaded chunk is discarded whenever its chunk loads again.
    private void respawnStoredHorse(ServerPlayer player, UUID playerId) {
        UUID horseId = HorseTracker.getLastRiddenId(playerId);
        if (horseId == null || HorseTracker.getSnapshot(horseId) == null) {
            // No usable last-ridden entry — fall back to any stored horse this player owns.
            horseId = HorseTracker.findStoredHorseOwnedBy(playerId);
        }
        if (horseId == null) {
            LOGGER.info("[whistle] no stored horse found for {}", playerId);
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        // Catch horses that are loaded but slipped past the tracker (e.g. spawn chunks loading before
        // server start); respawning on top of a loaded copy would collide on the entity UUID.
        if (level.getEntityInAnyDimension(horseId) instanceof AbstractHorse loaded) {
            LOGGER.info("[whistle] horse {} was loaded but untracked (level {})", horseId, loaded.level().dimension().identifier());
            if (loaded.level() == level && playerId.equals(((IHorseData) loaded).bh_getOwner())) {
                HorseTracker.register(loaded);
                summonHorseToPlayer(loaded, player);
            }
            return;
        }

        HorseTrackerState.KnownPosition known = HorseTracker.getLastKnownPosition(horseId);
        CompoundTag snapshot = HorseTracker.getSnapshot(horseId);
        if (known == null || snapshot == null) {
            LOGGER.info("[whistle] horse {} has no stored {} — cannot respawn",
                    horseId, snapshot == null ? "snapshot" : "position");
            return;
        }
        if (!level.dimension().equals(known.dimension())) {
            LOGGER.info("[whistle] horse {} is in {}, player is in {} — not respawning",
                    horseId, known.dimension().identifier(), level.dimension().identifier());
            return;
        }
        if (snapshot.getIntOr("BH_Bond", 0) <= 0) {
            LOGGER.info("[whistle] horse {} has no bond — not respawning", horseId);
            return; // same rule as the loaded path
        }

        Entity loaded = EntityType.loadEntityRecursive(
                snapshot, level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true), EntityProcessor.NOP);
        if (!(loaded instanceof AbstractHorse horse)) {
            LOGGER.warn("[whistle] snapshot of horse {} did not deserialize to a horse", horseId);
            return;
        }

        IHorseData data = (IHorseData) horse;
        int newGeneration = HorseTracker.getGeneration(horseId) + 1;
        data.bh_setGeneration(newGeneration);
        horse.snapTo(player.getX(), player.getY(), player.getZ(), horse.getYRot(), horse.getXRot());
        horse.fallDistance = 0.0F;
        data.bh_setCommand(HorseCommand.FOLLOW);

        if (!level.addFreshEntity(horse)) {
            LOGGER.warn("[whistle] failed to spawn respawned horse {}", horseId);
            return;
        }
        // Committed only after the spawn succeeded, otherwise the original copy would become stale
        // with no live replacement.
        HorseTracker.setGeneration(horseId, newGeneration);
        LOGGER.info("[whistle] respawned horse {} at {} (generation {})", horseId, player.blockPosition(), newGeneration);
    }

    // Resolve which horse the whistle summons: prefer the last horse this player rode, else the nearest owned horse in the same level.
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
                if (HorseTracker.isStale(horse)) {
                    // Leftover copy of a whistle-respawned horse; discard next tick, outside the load callback.
                    staleHorses.add(horse);
                } else {
                    HorseTracker.register(horse);
                }
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
            if (server.getTickCount() % PASSIVE_BOND_INTERVAL_TICKS == 0) {
                growHorseBond(server);
                HorseTracker.recordLoadedPositions();
            }
            discardStaleHorses();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(HorseTracker::attach);
        // Snapshot horses before the final world save so nothing is stale after a restart.
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HorseTracker.recordLoadedPositions());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            staleHorses.clear();
            HorseTracker.detach();
        });
    }

    private void discardStaleHorses() {
        if (staleHorses.isEmpty()) return;
        for (AbstractHorse stale : staleHorses) {
            if (!stale.isRemoved()) {
                LOGGER.debug("Discarding stale horse copy {} (generation {} < {})",
                        stale.getUUID(),
                        ((IHorseData) stale).bh_getGeneration(),
                        HorseTracker.getGeneration(stale.getUUID()));
                stale.discard();
            }
        }
        staleHorses.clear();
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
            return null;
        }
        if (!horse.isTamed()) {
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
