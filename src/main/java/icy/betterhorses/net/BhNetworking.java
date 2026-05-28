package icy.betterhorses.net;

import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.ClientPayloadHandlers;
import icy.betterhorses.net.network.OpenRadialPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.RequestOpenRadialPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
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

        CHANNEL.messageBuilder(RequestOpenRadialPayload.class, messageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestOpenRadialPayload::encode)
                .decoder(RequestOpenRadialPayload::decode)
                .consumerMainThread(BhNetworking::handleOpenRadialRequest)
                .add();

        CHANNEL.messageBuilder(OpenRadialPayload.class, messageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenRadialPayload::encode)
                .decoder(OpenRadialPayload::decode)
                .consumerMainThread(ClientPayloadHandlers::handleOpenRadial)
                .add();
    }

    public static void sendToServer(CallHorsePayload payload) {
        CHANNEL.sendToServer(payload);
    }

    public static void sendToServer(RequestOpenRadialPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    public static void sendToServer(RadialCommandPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, OpenRadialPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
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

    private static void handleOpenRadialRequest(RequestOpenRadialPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        IcysBetterHorses.LOGGER.info("[RADIAL][3] C2S received RequestOpenRadialPayload(horseId={}) from player {}",
                payload.horseId(), player.getName().getString());
        context.enqueueWork(() -> IcysBetterHorses.handleOpenRadialRequest(player, payload.horseId()));
        context.setPacketHandled(true);
    }
}
