package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class IcysBetterHorses implements ModInitializer {

    public static final String MOD_ID = "icys-better-horses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final int PASSIVE_BOND_INTERVAL_TICKS = 60 * 20;

    @Override
    public void onInitialize() {
        BhConfig.load();
        ModBlocks.init();
        ModBlockEntities.init();
        ModItems.init();
        ModSounds.init();
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
        if (horse == null) return;

        IHorseData data = (IHorseData) horse;
        if (data.bh_getBond() <= 0) return;

        // Whistling always cancels whatever standing order the horse was on — the player explicitly wants it to come to them.
        data.bh_setCommand(HorseCommand.FOLLOW);

        BlockPos target = player.blockPosition();
        if (horse.distanceToSqr(player) > 400.0) {
            horse.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        }
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
            if (server.getTickCount() % PASSIVE_BOND_INTERVAL_TICKS == 0) {
                growHorseBond(server);
            }
        });
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
