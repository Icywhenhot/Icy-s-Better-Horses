package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.OpenRadialPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.RequestOpenRadialPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class IcysBetterHorses implements ModInitializer {

    public static final String MOD_ID = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Identifier WATER_SPEED_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "water_speed");
    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModSounds.init();
        registerPackets();
        registerServerHandlers();
        registerEntityTracking();
        registerTickEvents();
        LOGGER.info("Icy's Better Horses initialized.");
    }

    private void registerPackets() {
        PayloadTypeRegistry.playC2S().register(RadialCommandPayload.TYPE, new RadialCommandPayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(CallHorsePayload.TYPE, new CallHorsePayload.StreamCodec());
        PayloadTypeRegistry.playC2S().register(RequestOpenRadialPayload.TYPE, new RequestOpenRadialPayload.StreamCodec());
        PayloadTypeRegistry.playS2C().register(OpenRadialPayload.TYPE, new OpenRadialPayload.StreamCodec());
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

        ServerPlayNetworking.registerGlobalReceiver(RequestOpenRadialPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            LOGGER.info("[RADIAL][3] C2S received RequestOpenRadialPayload(horseId={}) from player {}",
                    payload.horseId(), player.getName().getString());
            context.server().execute(() -> handleOpenRadialRequest(player, payload.horseId()));
        });
    }

    private void handleOpenRadialRequest(ServerPlayer player, int horseId) {
        LOGGER.info("[RADIAL][3a] handleOpenRadialRequest on main thread: player={}, horseId={}",
                player.getName().getString(), horseId);
        AbstractHorse horse = findCommandHorse(player, horseId, 12.0);
        if (horse == null) {
            LOGGER.info("[RADIAL][3z] Aborting: findCommandHorse returned null");
            return;
        }

        LOGGER.info("[RADIAL][4] Validation passed, sending OpenRadialPayload(horseId={}) back to player {}",
                horse.getId(), player.getName().getString());
        ServerPlayNetworking.send(player, new OpenRadialPayload(horse.getId()));
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
        AbstractHorse horse = HorseTracker.getLastRidden(playerId);
        if (horse == null) return;

        IHorseData data = (IHorseData) horse;
        if (!playerId.equals(data.bh_getOwner()) || data.bh_getBond() <= 0) return;

        BlockPos target = player.blockPosition();
        if (horse.distanceToSqr(player) > 400.0) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        } else {
            data.bh_setCommand(HorseCommand.FOLLOW);
        }
    }

    private void registerEntityTracking() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof AbstractHorse horse && ((IHorseData) horse).bh_isOwned()) {
                HorseTracker.register(horse);
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
            updateMountedWaterSpeed(server);
            if (server.getTickCount() % PASSIVE_BOND_INTERVAL_TICKS == 0) {
                growHorseBond(server);
            }
        });
    }

    private void updateMountedWaterSpeed(net.minecraft.server.MinecraftServer server) {
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
            LOGGER.info("[RADIAL][V1] Fail: entity id {} is not an AbstractHorse in player's level (got {})",
                    horseId,
                    serverLevel.getEntity(horseId) == null
                            ? "null"
                            : serverLevel.getEntity(horseId).getClass().getSimpleName());
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
