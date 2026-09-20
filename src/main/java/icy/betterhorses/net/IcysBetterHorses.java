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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
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
        ModEntities.registerAttributes();
        BhCriteria.register();
        BhHorseSpawnRules.installSpawnPlacementOverride();
        LOGGER.info("Icy's Better Horses initialized.");
    }

    // TODO: register once server events are ported.
    public void onServerStarted(MinecraftServer server) {
        HorseTracker.attach(server);
    }

    // TODO: register once server events are ported.
    public void onServerStopping() {
        HorseTracker.recordLoadedPositions();
    }

    // TODO: register once server events are ported.
    public void onServerStopped() {
        staleHorses.clear();
        pendingReleases.clear();
        HorseTracker.detach();
    }

    // TODO: register once server events are ported.
    public void onEntityJoinLevel(Entity entity, Level level) {
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
            if (HorseTracker.consumePendingDisown(horse.getUUID())) {
                pendingReleases.add(horse);
            } else if (HorseTracker.isStale(horse)) {
                staleHorses.add(horse);
            } else {
                HorseTracker.register(horse);
            }
        }
    }

    // TODO: register once server events are ported.
    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher,
                                    CommandBuildContext context,
                                    Commands.CommandSelection selection) {
        BhCommands.register(dispatcher, context, selection);
    }

    // TODO: register once server events are ported.
    public void onPlayerJoin(ServerPlayer player) {
        sendTrustList(player);
        BhNetworking.sendToPlayer(player, new ConfigSyncPayload(
                BhConfig.disabledFeatures(),
                BhConfig.classAbilitiesEnabled(),
                BhConfig.breedAbilitiesEnabled(),
                BhConfig.disabledAbilities(),
                BhConfig.tuning()));
        BhNetworking.sendToPlayer(player, BreedDataPayload.current());
    }

    // TODO: register once server events are ported.
    public void onDatapackSync(@Nullable ServerPlayer player, PlayerList playerList) {
        if (player != null) return;
        BreedDataPayload breeds = BreedDataPayload.current();
        playerList.getPlayers().forEach(target -> BhNetworking.sendToPlayer(target, breeds));
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

    // TODO: register once server events are ported.
    public void onEntityLeaveLevel(Entity entity) {
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
        if (!mount.level().isClientSide()) {
            BhSurge.pulse(IHorseData.of(mount), 0, 1);
        }
        return true;
    }

    // TODO: register once server events are ported.
    public void onAddReloadListener() {
        BhBreedLoader.register();
    }

    // TODO: register once server events are ported.
    public void onServerTick(MinecraftServer server) {
        BhTuning tuning = BhConfig.tuning();
        if (tuning.bondAmount() > 0 && server.getTickCount() % tuning.bondIntervalTicks() == 0) {
            growHorseBond(server, tuning.bondAmount());
        }
        HorseTracker.tick(server.getTickCount());
        discardStaleHorses();
        applyPendingReleases();
    }

    // TODO: register once server events are ported.
    public float onMountedBreakSpeed(Player player, float speed) {
        if (player.getVehicle() instanceof AbstractHorse) {
            return speed * 6.0F;
        }
        return speed;
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
        if (horse.distanceToSqr(player) > 400.0) {
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
