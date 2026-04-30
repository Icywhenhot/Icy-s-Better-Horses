package icy.betterhorses.net.fabric;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModBlockEntities;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.OpenRadialPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.RequestOpenRadialPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class IcysBetterHorsesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        IcysBetterHorses.init();
        ModBlockEntities.init();
        registerPackets();
        registerServerHandlers();
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> IcysBetterHorses.onHorseLoaded(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> IcysBetterHorses.onHorseUnloaded(entity));
        ServerTickEvents.END_SERVER_TICK.register(IcysBetterHorses::onServerTick);
    }

    private static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(RadialCommandPayload.TYPE, RadialCommandPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(CallHorsePayload.TYPE, CallHorsePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RequestOpenRadialPayload.TYPE, RequestOpenRadialPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenRadialPayload.TYPE, OpenRadialPayload.STREAM_CODEC);
    }

    private static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(RadialCommandPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            HorseCommand command = HorseCommand.fromId(payload.commandOrdinal());
            context.server().execute(() -> IcysBetterHorses.handleRadialCommand(player, payload.horseId(), command));
        });

        ServerPlayNetworking.registerGlobalReceiver(CallHorsePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> IcysBetterHorses.handleCallHorse(player));
        });

        ServerPlayNetworking.registerGlobalReceiver(RequestOpenRadialPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> IcysBetterHorses.handleOpenRadialRequest(
                    player,
                    payload.horseId(),
                    openHorseId -> ServerPlayNetworking.send(player, new OpenRadialPayload(openHorseId))));
        });
    }
}
