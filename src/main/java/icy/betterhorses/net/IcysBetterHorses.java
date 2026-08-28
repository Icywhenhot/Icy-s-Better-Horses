package icy.betterhorses.net;

import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.BhSteerModePayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.HorseGearPayload;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.OpenHorseRosterPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import icy.betterhorses.net.book.BhBookPages;
import icy.betterhorses.net.network.HorseRosterEntry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;

public class IcysBetterHorses implements ModInitializer {

    public static final String MOD_ID = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;

    private static final float COMMAND_ANSWER_CHANCE = 0.5F;

    private final List<AbstractHorse> staleHorses = new ArrayList<>();
    private final List<AbstractHorse> pendingReleases = new ArrayList<>();

    @Override
    public void onInitialize() {
        BhConfig.load();
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModItems.init();
        ModSounds.init();
        ModTicketTypes.init();
        BhBiomeSpawns.register();
        BhHorseSpawnRules.installSpawnPlacementOverride();
        BhCriteria.init();
        BhBookPages.init();
        BhCommands.register();
        registerPackets();
        registerServerHandlers();
        registerJoinSync();
        registerEntityTracking();
        registerTickEvents();
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void registerPackets() {
        PayloadTypeRegistry.serverboundPlay().register(RadialCommandPayload.TYPE, new RadialCommandPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(CallHorsePayload.TYPE, new CallHorsePayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(OpenHorseRosterPayload.TYPE, new OpenHorseRosterPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(HorseManagePayload.TYPE, new HorseManagePayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(HorseGearPayload.TYPE, new HorseGearPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(BhSteerModePayload.TYPE, new BhSteerModePayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(BhRearPayload.TYPE, new BhRearPayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(HorseRosterSyncPayload.TYPE, new HorseRosterSyncPayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(HorseManageResultPayload.TYPE, new HorseManageResultPayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(TrustSyncPayload.TYPE, new TrustSyncPayload.StreamCodec());
    }

    public static void sendTrustList(ServerPlayer player) {
        ServerPlayNetworking.send(player, new TrustSyncPayload(HorseTracker.getTrustingOwners(player.getUUID())));
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

        ServerPlayNetworking.registerGlobalReceiver(HorseGearPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    handleGearShift(player, payload.horseId(), payload.gear(), payload.gaitGear()));
        });

        ServerPlayNetworking.registerGlobalReceiver(BhSteerModePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    handleSteerMode(player, payload.horseId(), payload.freeSteer()));
        });

        ServerPlayNetworking.registerGlobalReceiver(BhRearPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleRear(player, payload.horseId()));
        });

        ServerPlayNetworking.registerGlobalReceiver(OpenHorseRosterPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> sendRoster(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(HorseManagePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            HorseManageAction action = HorseManageAction.fromId(payload.actionOrdinal());
            context.server().execute(() -> handleManageAction(player, payload.horseId(), action));
        });
    }

    private void sendRoster(ServerPlayer player) {
        List<HorseRosterEntry> roster = HorseManagement.buildRoster(player);
        ServerPlayNetworking.send(player, new HorseRosterSyncPayload(roster));
        if (!roster.isEmpty()) {
            BhCriteria.fire(player, BhCriteria.OWN_HORSE);
            BhCriteria.fire(player, BhCriteria.HORSE_COUNT, roster.size());
            for (HorseRosterEntry entry : roster) {
                BhCriteria.fireBreed(player, HorseBreed.fromId(entry.breedOrdinal()));
            }
        }
    }

    private void handleManageAction(ServerPlayer player, UUID horseId, HorseManageAction action) {
        HorseManagement.Outcome outcome = switch (action) {
            case WHISTLE -> HorseManagement.whistle(player, horseId);
            case SEND_HOME -> HorseManagement.sendHome(player, horseId);
            case DISOWN -> HorseManagement.disown(player, horseId);
            case SET_ACTIVE -> HorseManagement.setActive(player, horseId);
        };

        ServerPlayNetworking.send(player,
                new HorseManageResultPayload(horseId, action.ordinal(), outcome.ok(), outcome.messageKey()));
        if (outcome.ok()) {
            if (action == HorseManageAction.WHISTLE) {
                playWhistle(player);
            }
            sendRoster(player);
        }
    }

    private void handleRadialCommand(ServerPlayer player, int horseId, HorseCommand command) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) return;

        IHorseData data = IHorseData.of(horse);
        if (command == HorseCommand.SET_HOME) {
            data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(HorseCommand.STAY);
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.home_set"));
            BhCriteria.fire(player, BhCriteria.SET_HOME);
        } else {
            if (command == HorseCommand.WANDER) {
                data.bh_setWanderCenter(horse.blockPosition());
            }
            data.bh_setCommand(command);
        }

        playCommandAnswer(horse);
    }

    private static void playCommandAnswer(AbstractHorse horse) {
        if (horse.getRandom().nextFloat() >= COMMAND_ANSWER_CHANCE) {
            return;
        }
        SoundEvent sound = horse.getRandom().nextBoolean()
                ? ModSounds.HORSE_NEIGH
                : ModSounds.HORSE_SNORT;
        horse.level().playSound(
                null, horse.getX(), horse.getY(), horse.getZ(),
                sound, horse.getSoundSource(), 1.0F, 1.0F);
    }

    private void handleCallHorse(ServerPlayer player) {
        if (!(player.getVehicle() instanceof AbstractHorse)) {
            playWhistle(player);
        }

        HorseManagement.callNearestHorse(player);
    }

    private void playWhistle(ServerPlayer player) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.CALL_WHISTLE,
                SoundSource.PLAYERS,
                0.5F,
                1.0F);
    }

    private void registerJoinSync() {
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> sendTrustList(handler.getPlayer()));
    }

    private void registerEntityTracking() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof AbstractHorse horse && IHorseData.of(horse).bh_isOwned()) {
                if (HorseTracker.consumePendingDisown(horse.getUUID())) {
                    pendingReleases.add(horse);
                } else if (HorseTracker.isStale(horse)) {
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
            applyPendingReleases();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(HorseTracker::attach);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HorseTracker.recordLoadedPositions());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            staleHorses.clear();
            pendingReleases.clear();
            HorseTracker.detach();
        });
    }

    private void applyPendingReleases() {
        if (pendingReleases.isEmpty()) return;
        for (AbstractHorse horse : pendingReleases) {
            if (!horse.isRemoved()) {
                LOGGER.info("[manage] releasing horse {} disowned while unloaded", horse.getUUID());
                IHorseData.of(horse).bh_disown();
            }
        }
        pendingReleases.clear();
    }

    private void discardStaleHorses() {
        if (staleHorses.isEmpty()) return;
        for (AbstractHorse stale : staleHorses) {
            if (!stale.isRemoved()) {
                LOGGER.debug("Discarding stale horse copy {} (generation {} < {})",
                        stale.getUUID(),
                        IHorseData.of(stale).bh_getGeneration(),
                        HorseTracker.getGeneration(stale.getUUID()));
                stale.discard();
            }
        }
        staleHorses.clear();
    }

    private void growHorseBond(MinecraftServer server) {
        for (AbstractHorse horse : HorseTracker.getAll()) {
            IHorseData data = IHorseData.of(horse);
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

    private void handleGearShift(ServerPlayer player, int horseId, int gear, int gaitGear) {
        if (!(player.level().getEntity(horseId) instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player) {
            return;
        }
        IHorseData.of(horse).bh_setGear(gear);
        IHorseData.of(horse).bh_setGaitGear(gaitGear);
    }

    private void handleSteerMode(ServerPlayer player, int horseId, boolean freeSteer) {
        if (!(player.level().getEntity(horseId) instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player) {
            return;
        }
        IHorseData.of(horse).bh_setFreeSteer(freeSteer);
    }

    private void handleRear(ServerPlayer player, int horseId) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null || horse.isStanding()) {
            return;
        }
        if (horse.getControllingPassenger() != null && horse.getControllingPassenger() != player) {
            return;
        }
        horse.standIfPossible();
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

        if (!IHorseData.of(horse).bh_mayHandle(player.getUUID())) {
            return null;
        }

        return horse;
    }
}
