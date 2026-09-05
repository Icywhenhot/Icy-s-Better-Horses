package icy.betterhorses.net;

import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.BhFreeLookPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.HorseGearPayload;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
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

    private static final double CART_SIZE_REACH = 12.0D;
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
        ModMenus.init();
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
        PayloadTypeRegistry.serverboundPlay().register(HorseRecallPayload.TYPE, new HorseRecallPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(OpenHorseRosterPayload.TYPE, new OpenHorseRosterPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(HorseManagePayload.TYPE, new HorseManagePayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(HorseGearPayload.TYPE, new HorseGearPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(BhFreeLookPayload.TYPE, new BhFreeLookPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(BhRearPayload.TYPE, new BhRearPayload.StreamCodec());
        PayloadTypeRegistry.serverboundPlay().register(CartSizePayload.TYPE, new CartSizePayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(HorseRosterSyncPayload.TYPE, new HorseRosterSyncPayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(HorseManageResultPayload.TYPE, new HorseManageResultPayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(TrustSyncPayload.TYPE, new TrustSyncPayload.StreamCodec());
        PayloadTypeRegistry.clientboundPlay().register(HorseChargeShakePayload.TYPE, new HorseChargeShakePayload.StreamCodec());
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

        ServerPlayNetworking.registerGlobalReceiver(HorseRecallPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleRecall(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(HorseGearPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    handleGearShift(player, payload.horseId(), payload.gear(), payload.gaitGear()));
        });

        ServerPlayNetworking.registerGlobalReceiver(BhFreeLookPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() ->
                    handleFreeLook(player, payload.horseId(), payload.freeLook()));
        });

        ServerPlayNetworking.registerGlobalReceiver(BhRearPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleRear(player, payload.horseId()));
        });

        ServerPlayNetworking.registerGlobalReceiver(CartSizePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleCartSize(player, payload.targetId()));
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

    private static final int DISENGAGE_TICKS = 60;

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

    private void handleRecall(ServerPlayer player) {
        UUID ownerId = player.getUUID();
        boolean any = false;
        for (AbstractHorse horse : HorseTracker.getAll()) {
            IHorseData data = IHorseData.of(horse);
            if (!ownerId.equals(data.bh_getOwner()) || data.bh_getCombatState() == 0) {
                continue;
            }
            data.bh_setCombatTarget(null);
            data.bh_setSpookTicks(DISENGAGE_TICKS);
            any = true;
        }
        if (any) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.CALL_WHISTLE, player.getSoundSource(), 1.0F, 1.0F);
        }
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

            BhHorseTraits.grantBond(data, 1);
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

    private void handleFreeLook(ServerPlayer player, int horseId, boolean freeLook) {
        if (!(player.level().getEntity(horseId) instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player) {
            return;
        }
        IHorseData.of(horse).bh_setFreeLook(freeLook);
    }

    private void handleCartSize(ServerPlayer player, int targetId) {
        Entity target = player.level().getEntity(targetId);
        if (target == null || player.distanceToSqr(target) > CART_SIZE_REACH * CART_SIZE_REACH) {
            return;
        }

        if (target instanceof HorseCartEntity placed && placed.isPlaced()) {
            CartSize wanted = CartSize.byLarge(!placed.size().isLarge());
            if (refuseResize(player, placed, wanted)) {
                return;
            }
            placed.setSize(wanted);
            placed.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
            return;
        }

        AbstractHorse horse = target instanceof HorseCartEntity drawn
                ? drawn.boundHorse()
                : target instanceof AbstractHorse mount ? mount : null;
        if (horse == null) {
            return;
        }

        IHorseData data = IHorseData.of(horse);
        if (!data.bh_hasCartGear()) {
            return;
        }
        if (BhConfig.horseExclusivityEnabled() && !data.bh_mayHandle(player.getUUID())) {
            return;
        }

        CartSize wanted = CartSize.byLarge(!data.bh_hasLargeCart());
        if (wanted.isLarge() && !data.bh_mayUseLargeCart()) {
            player.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.cart_size_draft_only"));
            horse.playSound(SoundEvents.VILLAGER_NO, 1.0F, 1.0F);
            return;
        }

        HorseCartEntity cart = data.bh_getCartEntity();
        if (cart != null && refuseResize(player, cart, wanted)) {
            return;
        }

        data.bh_setLargeCart(wanted.isLarge());
        horse.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
    }

    private boolean refuseResize(ServerPlayer player, HorseCartEntity cart, CartSize wanted) {
        Component refusal = cart.resizeRefusal(wanted);
        if (refusal == null) {
            return false;
        }
        player.sendSystemMessage(refusal);
        return true;
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
