package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class BhNetworking {

    private static final String PROTOCOL_VERSION = "1";
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(IcysBetterHorses.MOD_ID, "main");
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(CHANNEL_ID)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int messageId = 0;

    private BhNetworking() {}

    public static void register() {
        CHANNEL.messageBuilder(RadialCommandPayload.class, messageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RadialCommandPayload::encode)
                .decoder(RadialCommandPayload::decode)
                .consumerMainThread(BhNetworking::handleRadialCommand)
                .add();

        CHANNEL.messageBuilder(CallHorsePayload.class, messageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CallHorsePayload::encode)
                .decoder(CallHorsePayload::decode)
                .consumerMainThread(BhNetworking::handleCallHorse)
                .add();
    }

    public static void sendToServer(CallHorsePayload payload) {
        CHANNEL.sendToServer(payload);
    }

    public static void sendToServer(RadialCommandPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    private static void handleRadialCommand(RadialCommandPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }
        HorseCommand command = HorseCommand.fromId(payload.commandOrdinal());
        context.enqueueWork(() -> IcysBetterHorses.handleRadialCommand(player, payload.horseId(), command));
        context.setPacketHandled(true);
    }

    private static void handleCallHorse(CallHorsePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> IcysBetterHorses.handleCallHorse(player));
        }
        context.setPacketHandled(true);
    }
}
