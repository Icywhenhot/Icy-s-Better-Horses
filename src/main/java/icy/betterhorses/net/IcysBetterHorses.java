package icy.betterhorses.net;

import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.feature.breed.Ironclad;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterEntry;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.CommandType;
import icy.betterhorses.net.registry.BhRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Mod(IcysBetterHorses.MOD_ID)
public final class IcysBetterHorses {

    public static final String MOD_ID = "icys_better_horses";
    public static final String RESOURCE_NAMESPACE = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final float COMMAND_ANSWER_CHANCE = 0.5F;
    private static final int DISENGAGE_TICKS = 60;
    private static final double CART_SIZE_REACH = 12.0D;
    private static final double DEFLECT_BOUNCE = 0.5D;

    private final List<AbstractHorse> staleHorses = new ArrayList<>();
    private final List<AbstractHorse> pendingReleases = new ArrayList<>();

    public IcysBetterHorses() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BhConfig.load();
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        BhNetworking.register();
        BhBiomeSpawns.register(modEventBus);
        modEventBus.addListener(this::registerSpawnPlacements);
        modEventBus.addListener(BhRegistries::onNewRegistry);
        BhContent.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(ModEntities::registerAttributes);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BhCriteria::register);
        event.enqueueWork(BhContent::logSummary);
        event.enqueueWork(BhBreedData::initializeBuiltIns);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        HorseTracker.attach(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        HorseTracker.recordLoadedPositions();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        staleHorses.clear();
        pendingReleases.clear();
        HorseTracker.detach();
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(
                EntityType.HORSE,
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BhHorseSpawnRules::checkHorseSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
            if (HorseTracker.consumePendingDisown(horse.getUUID())) {
                pendingReleases.add(horse);
            } else if (HorseTracker.isStale(horse)) {
                staleHorses.add(horse);
            } else {
                HorseTracker.register(horse);
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        BhCommands.register(event);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        sendTrustList(player);
        BhNetworking.sendToPlayer(player, new ConfigSyncPayload(
                BhConfig.disabledFeatures(),
                BhConfig.classAbilitiesEnabled(),
                BhConfig.breedAbilitiesEnabled(),
                BhConfig.disabledAbilities(),
                BhConfig.tuning()));
        BhNetworking.sendToPlayer(player, BreedDataPayload.current());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) return;
        BreedDataPayload breeds = BreedDataPayload.current();
        event.getPlayerList().getPlayers().forEach(player -> BhNetworking.sendToPlayer(player, breeds));
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

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof AbstractHorse horse) {
            HorseTracker.unregister(horse);
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)) {
            return;
        }
        Entity struck = hit.getEntity();
        AbstractHorse mount = null;
        if (struck instanceof AbstractHorse horse) {
            mount = horse;
        } else if (struck instanceof Player && struck.getVehicle() instanceof AbstractHorse ridden) {
            mount = ridden;
        }
        if (mount == null || !Ironclad.deflectsProjectiles(IHorseData.of(mount))) {
            return;
        }

        Projectile projectile = event.getProjectile();
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-DEFLECT_BOUNCE));
        projectile.setYRot(projectile.getYRot() + 180.0F);
        projectile.yRotO += 180.0F;
        projectile.hurtMarked = true;
        if (!mount.level().isClientSide()) {
            BhSurge.pulse(IHorseData.of(mount), 0, 1);
        }
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        BhBreedLoader.register(event);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();
        BhTuning tuning = BhConfig.tuning();
        if (tuning.bondAmount() > 0 && server.getTickCount() % tuning.bondIntervalTicks() == 0) {
            growHorseBond(server, tuning.bondAmount());
        }
        HorseTracker.tick(server.getTickCount());
        discardStaleHorses();
        applyPendingReleases();
    }

    @SubscribeEvent
    public void onMountedBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().getVehicle() instanceof AbstractHorse) {
            event.setNewSpeed(event.getNewSpeed() * 6.0F);
        }
    }

    public static void handleRadialCommand(ServerPlayer player, int horseId, ResourceKey<CommandType> command) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            return;
        }

        IHorseData data = (IHorseData) horse;
        if (command.equals(BhContent.COMMAND_ABILITY.getKey())) {
            if (CommandType.toggleable(data.bh_getBreedKey())) {
                boolean paused = !data.bh_isAbilityPaused();
                data.bh_setAbilityPaused(paused);
                player.sendSystemMessage(Component.translatable(paused
                        ? "message.icys-better-horses.ability_off"
                        : "message.icys-better-horses.ability_on"));
            } else {
                BreedAbility ability = ((IHorseAbilityHost) horse).bh_currentAbility();
                if (ability != null && ability.hasActiveSkill()) {
                    ability.onActivate(horse, data);
                }
            }
            playCommandAnswer(horse);
            return;
        }
        if (command.equals(BhContent.COMMAND_SET_HOME.getKey())) {
            data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(BhContent.COMMAND_STAY.getKey());
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.home_set"));
            BhCriteria.fire(player, BhCriteria.SET_HOME);
        } else {
            if (command.equals(BhContent.COMMAND_WANDER.getKey())) {
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
                ? ModSounds.HORSE_NEIGH.get()
                : ModSounds.HORSE_SNORT.get();
        horse.level().playSound(
                null, horse.getX(), horse.getY(), horse.getZ(),
                sound, horse.getSoundSource(), 1.0F, 1.0F);
    }

    public static void sendTrustList(ServerPlayer player) {
        BhNetworking.sendToPlayer(player,
                new TrustSyncPayload(HorseTracker.getTrustingOwners(player.getUUID())));
    }

    public static void handleRecall(ServerPlayer player) {
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
                    ModSounds.CALL_WHISTLE.get(), player.getSoundSource(), 1.0F, 1.0F);
        }
    }

    public static void sendRoster(ServerPlayer player) {
        List<HorseRosterEntry> roster = HorseManagement.buildRoster(player);
        BhNetworking.sendToPlayer(player, new HorseRosterSyncPayload(roster));
        if (!roster.isEmpty()) {
            BhCriteria.fire(player, BhCriteria.OWN_HORSE);
            BhCriteria.fire(player, BhCriteria.HORSE_COUNT, roster.size());
            for (HorseRosterEntry entry : roster) {
                BhCriteria.fireBreed(player, HorseBreed.byId(entry.breedId()));
            }
        }
    }

    public static void handleManageAction(ServerPlayer player, UUID horseId, HorseManageAction action) {
        HorseManagement.Outcome outcome = switch (action) {
            case WHISTLE -> HorseManagement.whistle(player, horseId);
            case SEND_HOME -> HorseManagement.sendHome(player, horseId);
            case DISOWN -> HorseManagement.disown(player, horseId);
            case SET_ACTIVE -> HorseManagement.setActive(player, horseId);
        };

        BhNetworking.sendToPlayer(player, new HorseManageResultPayload(
                horseId, action.ordinal(), outcome.ok(), outcome.messageKey()));
        if (outcome.ok()) {
            if (action == HorseManageAction.WHISTLE) {
                playWhistle(player);
            }
            sendRoster(player);
        }
    }

    public static void handleGearShift(ServerPlayer player, int horseId, int gear, int gaitGear) {
        if (!(player.level().getEntity(horseId) instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player) {
            return;
        }
        IHorseData.of(horse).bh_setGear(gear);
        IHorseData.of(horse).bh_setGaitGear(gaitGear);
    }

    public static void handleFreeLook(ServerPlayer player, int horseId, boolean freeLook) {
        if (!(player.level().getEntity(horseId) instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player) {
            return;
        }
        IHorseData.of(horse).bh_setFreeLook(freeLook);
    }

    public static void handleRear(ServerPlayer player, int horseId) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null || horse.isStanding()) {
            return;
        }
        if (horse.getControllingPassenger() != null && horse.getControllingPassenger() != player) {
            return;
        }
        horse.standIfPossible();
    }

    public static void handleCartSize(ServerPlayer player, int targetId) {
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

    private static boolean refuseResize(ServerPlayer player, HorseCartEntity cart, CartSize wanted) {
        Component refusal = cart.resizeRefusal(wanted);
        if (refusal == null) {
            return false;
        }
        player.sendSystemMessage(refusal);
        return true;
    }

    private static void playWhistle(ServerPlayer player) {
        player.level().playSound(
                null, player.getX(), player.getY(), player.getZ(),
                ModSounds.CALL_WHISTLE.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    public static void handleCallHorse(ServerPlayer player) {
        if (!(player.getVehicle() instanceof AbstractHorse)) {
            playWhistle(player);
        }

        HorseManagement.callNearestHorse(player);
    }

    private static void growHorseBond(MinecraftServer server, int amount) {
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

            BhHorseTraits.grantBond(data, amount);
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

        if (!IHorseData.of(horse).bh_mayHandle(player.getUUID())) {
            return null;
        }
        return horse;
    }
}
