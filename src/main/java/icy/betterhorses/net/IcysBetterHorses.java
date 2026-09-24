package icy.betterhorses.net;

import icy.betterhorses.net.entity.CartSize;
import icy.betterhorses.net.feature.breed.Ironclad;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterEntry;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class IcysBetterHorses implements ModInitializer {

    public static final String MOD_ID = "icys_better_horses";
    public static final String RESOURCE_NAMESPACE = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final float COMMAND_ANSWER_CHANCE = 0.35F;
    private static final int DISENGAGE_TICKS = 60;
    private static final double CART_SIZE_REACH = 6.0D;
    private static final double DEFLECT_BOUNCE = 0.5D;

    private final List<AbstractHorse> staleHorses = new ArrayList<>();
    private final List<AbstractHorse> pendingReleases = new ArrayList<>();

    @Override
    public void onInitialize() {
        BhConfig.load();
        ModBlocks.register();
        ModEntities.register();
        ModItems.register();
        ModMenus.register();
        ModSounds.register();
        ModAttachments.register();
        BhAttributes.register();
        ModEntities.registerAttributes();
        BhCriteria.register();
        BhHorseSpawnRules.installSpawnPlacementOverride();
        BhBiomeSpawns.register();
        BhNetworking.registerServer();

        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> onServerStopping());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> onServerStopped());
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
        ServerEntityEvents.ENTITY_LOAD.register(this::onEntityJoinLevel);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> onEntityLeaveLevel(entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.getPlayer()));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::onDatapackSync);
        CommandRegistrationCallback.EVENT.register(BhCommands::register);
        BhBreedLoader.register();
        bh_registerSpawnEggDispensers();

        LOGGER.info("Icy's Better Horses initialized.");
    }

    // Vanilla's own spawn-egg dispense behaviour is registered in DispenserBlock's static init,
    // which runs before our SpawnEggItem instances exist - so dispensers just no-op on our eggs
    // unless we register the same behaviour for them here.
    private static void bh_registerSpawnEggDispensers() {
        DispenseItemBehavior behavior = new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
                if (!(stack.getItem() instanceof SpawnEggItem eggItem)) {
                    return stack;
                }
                Direction facing = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos pos = blockSource.getPos().relative(facing);
                EntityType<?> type = eggItem.getType(new CompoundTag());
                type.spawn(blockSource.getLevel(), stack, null, pos, MobSpawnType.DISPENSER, true, false);
                stack.shrink(1);
                return stack;
            }
        };
        for (Item egg : ModItems.BREED_SPAWN_EGGS) {
            DispenserBlock.registerBehavior(egg, behavior);
        }
    }

    private void onServerStarted(MinecraftServer server) {
        HorseTracker.attach(server);
    }

    private void onServerStopping() {
        HorseTracker.recordLoadedPositions();
    }

    private void onServerStopped() {
        staleHorses.clear();
        pendingReleases.clear();
        HorseTracker.detach();
    }

    private void onEntityJoinLevel(Entity entity, Level level) {
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
            if (HorseTracker.isStale(horse)) {
                staleHorses.add(horse);
            } else if (HorseTracker.consumePendingDisown(horse.getUUID())) {
                pendingReleases.add(horse);
            } else {
                HorseTracker.register(horse);
            }
        }
    }

    private void onPlayerJoin(ServerPlayer player) {
        sendTrustList(player);
        BhNetworking.sendToPlayer(player, new ConfigSyncPayload(
                BhConfig.disabledFeatures(),
                BhConfig.classAbilitiesEnabled(),
                BhConfig.breedAbilitiesEnabled(),
                BhConfig.disabledAbilities(),
                BhConfig.tuning()));
        BhNetworking.sendToPlayer(player, BreedDataPayload.current());
    }

    private void onDatapackSync(ServerPlayer player, boolean joined) {
        if (joined) return;
        BhNetworking.sendToPlayer(player, BreedDataPayload.current());
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

    private void onEntityLeaveLevel(Entity entity) {
        if (entity instanceof AbstractHorse horse) {
            HorseTracker.unregister(horse);
        }
    }

    public static boolean bh_deflectProjectile(Projectile projectile, EntityHitResult hit) {
        Entity struck = hit.getEntity();
        AbstractHorse mount = null;
        if (struck instanceof AbstractHorse horse) {
            mount = horse;
        } else if (struck instanceof Player && struck.getVehicle() instanceof AbstractHorse ridden) {
            mount = ridden;
        }
        if (mount == null || !Ironclad.deflectsProjectiles(IHorseData.of(mount))) {
            return false;
        }

        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(-DEFLECT_BOUNCE));
        projectile.hurtMarked = true;
        if (projectile instanceof AbstractArrow arrow) {
            // pierce > 0 would re-find this same entity forever in AbstractArrow.tick's hit loop
            arrow.setPierceLevel((byte) 0);
        }
        if (!mount.level().isClientSide()) {
            BhSurge.pulse(IHorseData.of(mount), 0, 1);
        }
        return true;
    }

    private void onServerTick(MinecraftServer server) {
        BhTuning tuning = BhConfig.tuning();
        if (tuning.bondAmount() > 0 && server.getTickCount() % tuning.bondIntervalTicks() == 0) {
            growHorseBond(server, tuning.bondAmount());
        }
        HorseTracker.tick(server.getTickCount());
        discardStaleHorses();
        applyPendingReleases();
    }

    public static float bh_mountedBreakSpeed(Player player, float original) {
        if (player.getVehicle() instanceof AbstractHorse) {
            return original * 6.0F;
        }
        return original;
    }

    public static void handleRadialCommand(ServerPlayer player, int horseId, HorseCommand command) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            return;
        }

        IHorseData data = (IHorseData) horse;
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
                    ModSounds.CALL_WHISTLE, player.getSoundSource(), 1.0F, 1.0F);
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
                ModSounds.CALL_WHISTLE, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    public static void handleCallHorse(ServerPlayer player) {
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
            return;
        }

        IHorseData data = (IHorseData) horse;
        if (data.bh_getBond() <= 0) {
            return;
        }

        BlockPos target = player.blockPosition();
        if (BhFeature.HORSE_TELEPORT.on() && horse.distanceToSqr(player) > 400.0) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            data.bh_setWanderCenter(target);
            data.bh_setCommand(HorseCommand.WANDER);
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
        if (!(serverLevel.getEntity(horseId) instanceof AbstractHorse horse)
                || !BhHorseKind.managed(horse) || !horse.isTamed()) {
            return null;
        }
        if (horse.distanceToSqr(player) > radius * radius) {
            return null;
        }

        if (!((IHorseData) horse).bh_mayHandle(player.getUUID())) {
            return null;
        }
        return horse;
    }
}
