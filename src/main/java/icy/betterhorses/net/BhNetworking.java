package icy.betterhorses.net;

import icy.betterhorses.net.network.BhFreeLookPayload;
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseGearPayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.OpenHorseRosterPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BhNetworking {

    private static final String PROTOCOL_VERSION = "2";
    private static final ResourceLocation CHANNEL_ID =
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "main");
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(CHANNEL_ID)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int messageId = 0;

    private BhNetworking() {}

    public static void register() {
        toServer(RadialCommandPayload.class, RadialCommandPayload::encode, RadialCommandPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRadialCommand(
                        player, payload.horseId(), HorseCommand.fromId(payload.commandOrdinal())));
        toServer(CallHorsePayload.class, CallHorsePayload::encode, CallHorsePayload::decode,
                (payload, player) -> IcysBetterHorses.handleCallHorse(player));
        toServer(HorseRecallPayload.class, HorseRecallPayload::encode, HorseRecallPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRecall(player));
        toServer(OpenHorseRosterPayload.class, OpenHorseRosterPayload::encode, OpenHorseRosterPayload::decode,
                (payload, player) -> IcysBetterHorses.sendRoster(player));
        toServer(HorseManagePayload.class, HorseManagePayload::encode, HorseManagePayload::decode,
                (payload, player) -> IcysBetterHorses.handleManageAction(
                        player, payload.horseId(), HorseManageAction.fromId(payload.actionOrdinal())));
        toServer(HorseGearPayload.class, HorseGearPayload::encode, HorseGearPayload::decode,
                (payload, player) -> IcysBetterHorses.handleGearShift(
                        player, payload.horseId(), payload.gear(), payload.gaitGear()));
        toServer(BhFreeLookPayload.class, BhFreeLookPayload::encode, BhFreeLookPayload::decode,
                (payload, player) -> IcysBetterHorses.handleFreeLook(
                        player, payload.horseId(), payload.freeLook()));
        toServer(BhRearPayload.class, BhRearPayload::encode, BhRearPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRear(player, payload.horseId()));
        toServer(CartSizePayload.class, CartSizePayload::encode, CartSizePayload::decode,
                (payload, player) -> IcysBetterHorses.handleCartSize(player, payload.targetId()));

        toClient(HorseRosterSyncPayload.class, HorseRosterSyncPayload::encode, HorseRosterSyncPayload::decode,
                payload -> () -> IcysBetterHorsesClient.receiveHorseRoster(payload));
        toClient(HorseManageResultPayload.class, HorseManageResultPayload::encode, HorseManageResultPayload::decode,
                payload -> () -> IcysBetterHorsesClient.receiveManageResult(payload));
        toClient(TrustSyncPayload.class, TrustSyncPayload::encode, TrustSyncPayload::decode,
                payload -> () -> IcysBetterHorsesClient.receiveTrust(payload));
        toClient(HorseChargeShakePayload.class, HorseChargeShakePayload::encode, HorseChargeShakePayload::decode,
                payload -> () -> IcysBetterHorsesClient.receiveChargeShake(payload));
        toClient(ConfigSyncPayload.class, ConfigSyncPayload::encode, ConfigSyncPayload::decode,
                payload -> () -> IcysBetterHorsesClient.receiveConfig(payload));
        toClient(BreedDataPayload.class, BreedDataPayload::encode, BreedDataPayload::decode,
                payload -> () -> IcysBetterHorsesClient.receiveBreeds(payload));
    }

    public static void sendToServer(Object payload) {
        CHANNEL.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, Object payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    private static <T> void toServer(Class<T> type, BiConsumer<T, net.minecraft.network.FriendlyByteBuf> encoder,
                                     Function<net.minecraft.network.FriendlyByteBuf, T> decoder,
                                     BiConsumer<T, ServerPlayer> handler) {
        CHANNEL.messageBuilder(type, messageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(encoder)
                .decoder(decoder)
                .consumerMainThread((payload, ctx) -> {
                    NetworkEvent.Context context = ctx.get();
                    ServerPlayer sender = context.getSender();
                    if (sender != null) {
                        handler.accept(payload, sender);
                    }
                    context.setPacketHandled(true);
                })
                .add();
    }

    private static <T> void toClient(Class<T> type, BiConsumer<T, net.minecraft.network.FriendlyByteBuf> encoder,
                                     Function<net.minecraft.network.FriendlyByteBuf, T> decoder,
                                     Function<T, Runnable> clientHandler) {
        CHANNEL.messageBuilder(type, messageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(encoder)
                .decoder(decoder)
                .consumerMainThread((payload, ctx) -> {
                    NetworkEvent.Context context = ctx.get();
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> clientHandler.apply(payload));
                    context.setPacketHandled(true);
                })
                .add();
    }
}
