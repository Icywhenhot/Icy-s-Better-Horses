package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class IcysBetterHorses {

    public static final String MOD_ID = "icys-better-horses";
    public static final String NEOFORGE_MOD_ID = "icys_better_horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ResourceLocation WATER_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "water_speed");
    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;

    public static String registryOwnerId() {
        return dev.architectury.platform.Platform.isNeoForge() ? NEOFORGE_MOD_ID : MOD_ID;
    }

    public static void init() {
        ModBlocks.init();
        ModItems.init();
        ModSounds.init();
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private IcysBetterHorses() {
    }

    public static void onHorseLoaded(Entity entity) {
        if (entity instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
            HorseTracker.register(horse);
        }
    }

    public static void onHorseUnloaded(Entity entity) {
        if (entity instanceof AbstractHorse horse) {
            HorseTracker.unregister(horse);
        }
    }

    public static void onServerTick(net.minecraft.server.MinecraftServer server) {
        updateMountedWaterSpeed(server);
        if (server.getTickCount() % PASSIVE_BOND_INTERVAL_TICKS == 0) {
            growHorseBond(server);
        }
    }

    public static void handleOpenRadialRequest(ServerPlayer player, int horseId, java.util.function.IntConsumer openScreenSender) {
        LOGGER.info("[RADIAL][3a] handleOpenRadialRequest on main thread: player={}, horseId={}",
                player.getName().getString(), horseId);
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            LOGGER.info("[RADIAL][3z] Aborting: findCommandHorse returned null");
            return;
        }

        LOGGER.info("[RADIAL][4] Validation passed, sending OpenRadialPayload(horseId={}) back to player {}",
                horse.getId(), player.getName().getString());
        openScreenSender.accept(horse.getId());
    }

    public static void handleRadialCommand(ServerPlayer player, int horseId, HorseCommand command) {
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) return;

        IHorseData data = (IHorseData) horse;
        if (command == HorseCommand.SET_HOME) {
            data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(HorseCommand.STAY);
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.home_set"));
        } else {
            data.bh_setCommand(command);
        }
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
        BlockPos target = player.blockPosition();
        for (AbstractHorse horse : HorseTracker.getAll()) {
            if (!playerId.equals(((IHorseData) horse).bh_getOwner())) continue;
            if (horse.distanceToSqr(player) > 400.0) {
                horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
            } else {
                ((IHorseData) horse).bh_setCommand(HorseCommand.FOLLOW);
            }
        }
    }

    private static void updateMountedWaterSpeed(net.minecraft.server.MinecraftServer server) {
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

    private static void growHorseBond(net.minecraft.server.MinecraftServer server) {
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

    private static AbstractHorse findCommandHorse(ServerPlayer player, int horseId, double radius) {
        var entity = player.level().getEntity(horseId);
        if (!(entity instanceof AbstractHorse horse)) {
            LOGGER.info("[RADIAL][V1] Fail: entity id {} is not an AbstractHorse in player's level (got {})",
                    horseId,
                    entity == null ? "null" : entity.getClass().getSimpleName());
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
}
