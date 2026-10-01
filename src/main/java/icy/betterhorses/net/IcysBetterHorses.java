package icy.betterhorses.net;

import icy.betterhorses.net.network.BhRearPayload;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.Items;
import icy.betterhorses.net.network.BhChargePayload;
import icy.betterhorses.net.feature.HorseCombat;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.network.CartMenuPayload;
import icy.betterhorses.net.network.BhFreeLookPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.HorseGearPayload;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseJumpPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.OpenHorseRosterPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.CommandType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
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

    private static final double CART_REACH = 12.0D;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final float COMMAND_ANSWER_CHANCE = 0.5F;

    private final List<AbstractHorse> staleHorses = new ArrayList<>();
    private final List<AbstractHorse> pendingReleases = new ArrayList<>();

    @Override
    public void onInitialize() {
        BhConfig.load();
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(Items.LEATHER_HORSE_ARMOR, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(15)));
            context.modify(Items.COPPER_HORSE_ARMOR, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(8)));
            context.modify(Items.IRON_HORSE_ARMOR, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(9)));
            context.modify(Items.GOLDEN_HORSE_ARMOR, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(25)));
            context.modify(Items.DIAMOND_HORSE_ARMOR, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(10)));
            context.modify(Items.NETHERITE_HORSE_ARMOR, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(15)));
        });
        ModSounds.init();
        ModMenus.init();
        ModTicketTypes.init();
        BhContent.init();
        BhContent.logSummary();
        BhBiomeSpawns.register();
        BhBreedLoader.register();
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
        PayloadTypeRegistry.playC2S().register(RadialCommandPayload.TYPE, new RadialCommandPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(CallHorsePayload.TYPE, new CallHorsePayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(HorseRecallPayload.TYPE, new HorseRecallPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(OpenHorseRosterPayload.TYPE, new OpenHorseRosterPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(HorseManagePayload.TYPE, new HorseManagePayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(HorseGearPayload.TYPE, new HorseGearPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(BhFreeLookPayload.TYPE, new BhFreeLookPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(BhRearPayload.TYPE, new BhRearPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(CartMenuPayload.TYPE, new CartMenuPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(BhChargePayload.TYPE, new BhChargePayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(HorseRosterSyncPayload.TYPE, new HorseRosterSyncPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(HorseManageResultPayload.TYPE, new HorseManageResultPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(TrustSyncPayload.TYPE, new TrustSyncPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, new ConfigSyncPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(BreedDataPayload.TYPE, new BreedDataPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(HorseChargeShakePayload.TYPE, new HorseChargeShakePayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(HorseJumpPayload.TYPE, new HorseJumpPayload.StreamCodec());
    }

    private static ResourceKey<CommandType> bh_parseCommand(String raw) {
        Identifier loc = Identifier.tryParse(raw);
        return loc != null
                ? ResourceKey.create(BhRegistries.COMMAND_TYPES, loc)
                : null;
    }

    public static void sendTrustList(ServerPlayer player) {
        ServerPlayNetworking.send(player, new TrustSyncPayload(HorseTracker.getTrustingOwners(player.getUUID())));
    }

    private void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(RadialCommandPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            ResourceKey<CommandType> command = bh_parseCommand(payload.commandId());
            context.server().execute(() -> handleRadialCommand(player, payload.horseId(), command, payload.abilityId()));
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

        ServerPlayNetworking.registerGlobalReceiver(CartMenuPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleOpenCart(player, payload.targetId()));
        });

        ServerPlayNetworking.registerGlobalReceiver(BhChargePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> HorseCombat.riderCharge(player.getUUID(), payload.on()));
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
                Identifier breedId = Identifier.tryParse(entry.breedId());
                BhCriteria.fireBreed(player, HorseBreed.byId(breedId != null ? breedId.getPath() : entry.breedId()));
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

    public static void handleRadialCommand(ServerPlayer player, int horseId, ResourceKey<CommandType> command) {
        handleRadialCommand(player, horseId, command, "");
    }

    public static void handleRadialCommand(ServerPlayer player, int horseId,
                                          ResourceKey<CommandType> command, String abilityId) {
        if (command == null) return;
        CommandType type = BhRegistries.commandTypeRegistry().getValue(command.identifier());
        if (type == null) return;
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) return;

        if (type.custom()) {
            if (type.execute(horse, player)) playCommandAnswer(horse);
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (command.equals(BhContent.COMMAND_ABILITY.key())) {
            IHorseAbilityHost host = (IHorseAbilityHost) horse;
            if (!abilityId.isEmpty()) {
                Identifier id = Identifier.tryParse(abilityId);
                if (id == null || !host.bh_activateAbility(ResourceKey.create(BhRegistries.ABILITY_TYPES, id))) return;
            } else if (CommandType.toggleable(data.bh_getBreedKey())) {
                boolean paused = !data.bh_isAbilityPaused();
                data.bh_setAbilityPaused(paused);
                player.sendSystemMessage(Component.translatable(paused
                        ? "message.icys-better-horses.ability_off"
                        : "message.icys-better-horses.ability_on"));
            } else {
                var active = host.bh_activeAbilities();
                if (active.isEmpty() || !host.bh_activateAbility(active.get(0))) return;
            }
            playCommandAnswer(horse);
            return;
        }
        if (command.equals(BhContent.COMMAND_SET_HOME.key())) {
            data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(BhContent.COMMAND_STAY.key());
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.home_set"));
            BhCriteria.fire(player, BhCriteria.SET_HOME);
        } else {
            if (command.equals(BhContent.COMMAND_WANDER.key())) {
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
                (handler, sender, server) -> {
                    sendTrustList(handler.getPlayer());
                    ServerPlayNetworking.send(handler.getPlayer(), new ConfigSyncPayload(
                            BhConfig.disabledFeatures(),
                            BhConfig.classAbilitiesEnabled(),
                            BhConfig.breedAbilitiesEnabled(),
                            BhConfig.disabledAbilities(),
                            BhConfig.tuning()));
                    ServerPlayNetworking.send(handler.getPlayer(), BreedDataPayload.current());
                });
    }

    private void registerEntityTracking() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof LightningBolt bolt) {
                BhHorseCombatAlert.startle(world, bolt.position());
                return;
            }
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
            BhTuning tuning = BhConfig.tuning();
            if (tuning.bondAmount() > 0 && server.getTickCount() % tuning.bondIntervalTicks() == 0) {
                growHorseBond(server, tuning.bondAmount());
            }
            HorseTracker.tick(server.getTickCount());
            BhTwinWatch.tick(server, server.getTickCount());
            discardStaleHorses();
            applyPendingReleases();
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (!success) return;
            BreedDataPayload breeds = BreedDataPayload.current();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, breeds);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(HorseTracker::attach);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> HorseTracker.recordLoadedPositions());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            staleHorses.clear();
            pendingReleases.clear();
            BhTwinWatch.reset();
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
                LOGGER.info("[whistle] discarding stale horse copy {} on load (generation {} < {})",
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

    private void handleOpenCart(ServerPlayer player, int targetId) {
        Entity target = player.level().getEntity(targetId);
        if (target == null || player.distanceToSqr(target) > CART_REACH * CART_REACH) {
            return;
        }
        HorseCartEntity cart = target instanceof HorseCartEntity found ? found
                : target instanceof AbstractHorse horse && IHorseData.of(horse).bh_hasCartGear()
                ? IHorseData.of(horse).bh_getCartEntity()
                : null;
        if (cart != null) {
            cart.openMenu(player);
        }
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
        if (horse.isStanding() && BhFeature.REAR_NEIGH.on()) {
            horse.playSound(ModSounds.HORSE_NEIGH, 1.0F, 1.0F);
        }
    }

    private static AbstractHorse findCommandHorse(ServerPlayer player, int horseId, double radius) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        if (!(serverLevel.getEntity(horseId) instanceof AbstractHorse horse) || !BhHorseKind.managed(horse)) {
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
