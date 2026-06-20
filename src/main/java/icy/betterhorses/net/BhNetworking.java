package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class BhNetworking {

    private BhNetworking() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RadialCommandPayload.TYPE, RadialCommandPayload.STREAM_CODEC, BhNetworking::handleRadialCommand);
        registrar.playToServer(CallHorsePayload.TYPE, CallHorsePayload.STREAM_CODEC, BhNetworking::handleCallHorse);
    }

    private static void handleRadialCommand(RadialCommandPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        HorseCommand command = HorseCommand.fromId(payload.commandOrdinal());
        context.enqueueWork(() -> IcysBetterHorses.handleRadialCommand(player, payload.horseId(), command));
    }

    private static void handleCallHorse(CallHorsePayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> IcysBetterHorses.handleCallHorse(player));
    }
}
