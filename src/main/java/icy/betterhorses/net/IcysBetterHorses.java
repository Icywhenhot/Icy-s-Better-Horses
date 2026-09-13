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
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import icy.betterhorses.net.book.BhBookPages;
import icy.betterhorses.net.network.HorseRosterEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;

@Mod(IcysBetterHorses.NEO_ID)
public class IcysBetterHorses {

    public static final String MOD_ID = "icys-better-horses";
    public static final String NEO_ID = "icys_better_horses";

    private static final double CART_SIZE_REACH = 12.0D;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final float COMMAND_ANSWER_CHANCE = 0.5F;

    private final List<AbstractHorse> staleHorses = new ArrayList<>();
    private final List<AbstractHorse> pendingReleases = new ArrayList<>();

    public IcysBetterHorses(IEventBus modEventBus, ModContainer modContainer) {
        BhConfig.load();
        BhBookPages.init();
        modEventBus.addListener(ModBlocks::register);
        modEventBus.addListener(ModBlockEntities::register);
        modEventBus.addListener(ModEntities::register);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModItems::register);
        modEventBus.addListener(ModSounds::register);
        modEventBus.addListener(ModMenus::register);
        modEventBus.addListener(ModTicketTypes::register);
        modEventBus.addListener(BhHorseAttachments::register);
        modEventBus.addListener(BhBiomeSpawns::register);
        modEventBus.addListener(BhCriteria::register);
        modEventBus.addListener(this::registerPackets);
        modEventBus.addListener(this::registerSpawnPlacements);
        NeoForge.EVENT_BUS.addListener(BhBreedLoader::register);
        NeoForge.EVENT_BUS.addListener(BhCommands::register);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(this::onEntityLeave);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerStopping);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToServer(RadialCommandPayload.TYPE, new RadialCommandPayload.StreamCodec(), (payload, context) ->
                handleRadialCommand((ServerPlayer) context.player(), payload.horseId(), HorseCommand.fromId(payload.commandOrdinal())));
        registrar.playToServer(CallHorsePayload.TYPE, new CallHorsePayload.StreamCodec(), (payload, context) ->
                handleCallHorse((ServerPlayer) context.player()));
        registrar.playToServer(HorseRecallPayload.TYPE, new HorseRecallPayload.StreamCodec(), (payload, context) ->
                handleRecall((ServerPlayer) context.player()));
        registrar.playToServer(OpenHorseRosterPayload.TYPE, new OpenHorseRosterPayload.StreamCodec(), (payload, context) ->
                sendRoster((ServerPlayer) context.player()));
        registrar.playToServer(HorseManagePayload.TYPE, new HorseManagePayload.StreamCodec(), (payload, context) ->
                handleManageAction((ServerPlayer) context.player(), payload.horseId(), HorseManageAction.fromId(payload.actionOrdinal())));
        registrar.playToServer(HorseGearPayload.TYPE, new HorseGearPayload.StreamCodec(), (payload, context) ->
                handleGearShift((ServerPlayer) context.player(), payload.horseId(), payload.gear(), payload.gaitGear()));
        registrar.playToServer(BhFreeLookPayload.TYPE, new BhFreeLookPayload.StreamCodec(), (payload, context) ->
                handleFreeLook((ServerPlayer) context.player(), payload.horseId(), payload.freeLook()));
        registrar.playToServer(BhRearPayload.TYPE, new BhRearPayload.StreamCodec(), (payload, context) ->
                handleRear((ServerPlayer) context.player(), payload.horseId()));
        registrar.playToServer(CartSizePayload.TYPE, new CartSizePayload.StreamCodec(), (payload, context) ->
                handleCartSize((ServerPlayer) context.player(), payload.targetId()));
        registrar.playToClient(HorseRosterSyncPayload.TYPE, new HorseRosterSyncPayload.StreamCodec());
        registrar.playToClient(HorseManageResultPayload.TYPE, new HorseManageResultPayload.StreamCodec());
        registrar.playToClient(TrustSyncPayload.TYPE, new TrustSyncPayload.StreamCodec());
        registrar.playToClient(ConfigSyncPayload.TYPE, new ConfigSyncPayload.StreamCodec());
        registrar.playToClient(BreedDataPayload.TYPE, new BreedDataPayload.StreamCodec());
        registrar.playToClient(HorseChargeShakePayload.TYPE, new HorseChargeShakePayload.StreamCodec());
    }

    public static void sendTrustList(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new TrustSyncPayload(HorseTracker.getTrustingOwners(player.getUUID())));
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                EntityType.HORSE,
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BhHorseSpawnRules::checkHorseSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private void sendRoster(ServerPlayer player) {
        List<HorseRosterEntry> roster = HorseManagement.buildRoster(player);
        PacketDistributor.sendToPlayer(player, new HorseRosterSyncPayload(roster));
        if (!roster.isEmpty()) {
            BhCriteria.fire(player, BhCriteria.OWN_HORSE);
            BhCriteria.fire(player, BhCriteria.HORSE_COUNT, roster.size());
            for (HorseRosterEntry entry : roster) {
                BhCriteria.fireBreed(player, HorseBreed.byId(entry.breedId()));
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

        PacketDistributor.sendToPlayer(player,
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
        if (command == HorseCommand.ABILITY) {
            boolean paused = !data.bh_isAbilityPaused();
            data.bh_setAbilityPaused(paused);
            player.sendSystemMessage(Component.translatable(paused
                    ? "message.icys-better-horses.ability_off"
                    : "message.icys-better-horses.ability_on"));
            playCommandAnswer(horse);
            return;
        }
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

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        sendTrustList(player);
        PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(
                BhConfig.disabledFeatures(),
                BhConfig.classAbilitiesEnabled(),
                BhConfig.breedAbilitiesEnabled(),
                BhConfig.disabledAbilities(),
                BhConfig.tuning()));
        PacketDistributor.sendToPlayer(player, BreedDataPayload.current());
    }

    private void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) return;
        BreedDataPayload breeds = BreedDataPayload.current();
        event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, breeds));
    }

    private void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof AbstractHorse horse && IHorseData.of(horse).bh_isOwned()) {
            if (HorseTracker.consumePendingDisown(horse.getUUID())) {
                pendingReleases.add(horse);
            } else if (HorseTracker.isStale(horse)) {
                staleHorses.add(horse);
            } else {
                HorseTracker.register(horse);
            }
        }
    }

    private void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof AbstractHorse horse) {
            HorseTracker.unregister(horse);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        BhTuning tuning = BhConfig.tuning();
        if (tuning.bondAmount() > 0 && server.getTickCount() % tuning.bondIntervalTicks() == 0) {
            growHorseBond(server, tuning.bondAmount());
        }
        HorseTracker.tick(server.getTickCount());
        discardStaleHorses();
        applyPendingReleases();
    }

    private void onServerStarted(ServerStartedEvent event) {
        HorseTracker.attach(event.getServer());
    }

    private void onServerStopping(ServerStoppingEvent event) {
        HorseTracker.recordLoadedPositions();
    }

    private void onServerStopped(ServerStoppedEvent event) {
        staleHorses.clear();
        pendingReleases.clear();
        HorseTracker.detach();
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

    private void growHorseBond(MinecraftServer server, int amount) {
        for (AbstractHorse horse : HorseTracker.getAll()) {
            IHorseData data = IHorseData.of(horse);
            if (data.bh_getBond() >= 100) continue;

            UUID ownerId = data.bh_getOwner();
            if (ownerId == null) continue;

            ServerPlayer owner = server.getPlayerList().getPlayer(ownerId);
            if (owner == null || owner.level() != horse.level() || horse.distanceToSqr(owner) >= 100.0) {
                continue;
            }

            BhHorseTraits.grantBond(data, amount);
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
        if (cart != null) {
            if (refuseResize(player, cart, wanted)) {
                return;
            }
        } else if (data.bh_hasCartChest()
                && HorseCartEntity.itemsBeyond(data.bh_getCartChestContainer(), wanted.chestSlots())) {
            player.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.cart_size_chest_full"));
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
