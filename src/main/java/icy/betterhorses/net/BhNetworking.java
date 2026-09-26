package icy.betterhorses.net;

import icy.betterhorses.net.network.BhFreeLookPayload;
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseGearPayload;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.OpenHorseRosterPayload;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Replaces the Forge SimpleChannel, with one channel per payload type.
 * Receivers run off the game thread, so every handler hops back to it before touching game state.
 */
public final class BhNetworking {

    private static final Map<Class<?>, ResourceLocation> CHANNELS = new HashMap<>();
    private static final Map<Class<?>, BiConsumer<Object, FriendlyByteBuf>> ENCODERS = new HashMap<>();

    private BhNetworking() {}

    private static ResourceLocation id(String path) {
        return new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path);
    }

    @SuppressWarnings("unchecked")
    private static <T> void channel(Class<T> type, String path, BiConsumer<T, FriendlyByteBuf> encoder) {
        CHANNELS.put(type, id(path));
        ENCODERS.put(type, (BiConsumer<Object, FriendlyByteBuf>) encoder);
    }

    static {
        // Server-bound (client -> server)
        channel(RadialCommandPayload.class, "radial_command", RadialCommandPayload::encode);
        channel(CallHorsePayload.class, "call_horse", CallHorsePayload::encode);
        channel(HorseRecallPayload.class, "horse_recall", HorseRecallPayload::encode);
        channel(OpenHorseRosterPayload.class, "open_horse_roster", OpenHorseRosterPayload::encode);
        channel(HorseManagePayload.class, "horse_manage", HorseManagePayload::encode);
        channel(HorseGearPayload.class, "horse_gear", HorseGearPayload::encode);
        channel(BhFreeLookPayload.class, "free_look", BhFreeLookPayload::encode);
        channel(BhRearPayload.class, "rear", BhRearPayload::encode);
        channel(CartSizePayload.class, "cart_size", CartSizePayload::encode);

        // Client-bound (server -> client)
        channel(HorseRosterSyncPayload.class, "horse_roster_sync", HorseRosterSyncPayload::encode);
        channel(HorseManageResultPayload.class, "horse_manage_result", HorseManageResultPayload::encode);
        channel(TrustSyncPayload.class, "trust_sync", TrustSyncPayload::encode);
        channel(HorseChargeShakePayload.class, "charge_shake", HorseChargeShakePayload::encode);
        channel(ConfigSyncPayload.class, "config_sync", ConfigSyncPayload::encode);
        channel(BreedDataPayload.class, "breed_data", BreedDataPayload::encode);
    }

    public static void registerServer() {
        toServer(RadialCommandPayload.class, RadialCommandPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRadialCommand(
                        player, payload.horseId(), HorseCommand.fromId(payload.commandOrdinal())));
        toServer(CallHorsePayload.class, CallHorsePayload::decode,
                (payload, player) -> IcysBetterHorses.handleCallHorse(player));
        toServer(HorseRecallPayload.class, HorseRecallPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRecall(player));
        toServer(OpenHorseRosterPayload.class, OpenHorseRosterPayload::decode,
                (payload, player) -> IcysBetterHorses.sendRoster(player));
        toServer(HorseManagePayload.class, HorseManagePayload::decode,
                (payload, player) -> IcysBetterHorses.handleManageAction(
                        player, payload.horseId(), HorseManageAction.fromId(payload.actionOrdinal())));
        toServer(HorseGearPayload.class, HorseGearPayload::decode,
                (payload, player) -> IcysBetterHorses.handleGearShift(
                        player, payload.horseId(), payload.gear(), payload.gaitGear()));
        toServer(BhFreeLookPayload.class, BhFreeLookPayload::decode,
                (payload, player) -> IcysBetterHorses.handleFreeLook(
                        player, payload.horseId(), payload.freeLook()));
        toServer(BhRearPayload.class, BhRearPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRear(player, payload.horseId()));
        toServer(CartSizePayload.class, CartSizePayload::decode,
                (payload, player) -> IcysBetterHorses.handleCartSize(player, payload.targetId()));
    }

    public static void registerClient() {
        toClient(HorseRosterSyncPayload.class, HorseRosterSyncPayload::decode,
                IcysBetterHorsesClient::receiveHorseRoster);
        toClient(HorseManageResultPayload.class, HorseManageResultPayload::decode,
                IcysBetterHorsesClient::receiveManageResult);
        toClient(TrustSyncPayload.class, TrustSyncPayload::decode,
                IcysBetterHorsesClient::receiveTrust);
        toClient(HorseChargeShakePayload.class, HorseChargeShakePayload::decode,
                IcysBetterHorsesClient::receiveChargeShake);
        toClient(ConfigSyncPayload.class, ConfigSyncPayload::decode,
                IcysBetterHorsesClient::receiveConfig);
        toClient(BreedDataPayload.class, BreedDataPayload::decode,
                IcysBetterHorsesClient::receiveBreeds);
    }

    private static <T> void toServer(Class<T> type, Function<FriendlyByteBuf, T> decoder,
                                     BiConsumer<T, ServerPlayer> handler) {
        ResourceLocation channel = CHANNELS.get(type);
        boolean registered = ServerPlayNetworking.registerGlobalReceiver(channel,
                (server, player, listener, buf, sender) -> {
                    T payload = decoder.apply(buf);
                    server.execute(() -> handler.accept(payload, player));
                });
        if (!registered) {
            throw new IllegalStateException(
                    "Better Horses channel already has a server receiver: " + channel);
        }
    }

    private static <T> void toClient(Class<T> type, Function<FriendlyByteBuf, T> decoder,
                                     Consumer<T> handler) {
        ResourceLocation channel = CHANNELS.get(type);
        boolean registered = ClientPlayNetworking.registerGlobalReceiver(channel,
                (client, listener, buf, sender) -> {
                    T payload = decoder.apply(buf);
                    client.execute(() -> handler.accept(payload));
                });
        if (!registered) {
            throw new IllegalStateException(
                    "Better Horses channel already has a client receiver: " + channel);
        }
    }

    public static void sendToServer(Object payload) {
        ClientPlayNetworking.send(channelOf(payload), write(payload));
    }

    /**
     * Sends a payload, skipping clients without the mod's channel (e.g. vanilla clients).
     * Fabric has no login version check like Forge's, so mismatched clients aren't kicked.
     */
    public static void sendToPlayer(ServerPlayer player, Object payload) {
        ResourceLocation channel = channelOf(payload);
        if (!ServerPlayNetworking.canSend(player, channel)) {
            IcysBetterHorses.LOGGER.debug(
                    "Skipping {} for {}: client has not registered channel {}",
                    payload.getClass().getSimpleName(), player.getGameProfile().getName(), channel);
            return;
        }
        ServerPlayNetworking.send(player, channel, write(payload));
    }

    private static ResourceLocation channelOf(Object payload) {
        ResourceLocation channel = CHANNELS.get(payload.getClass());
        if (channel == null) {
            throw new IllegalArgumentException(
                    "No Better Horses channel registered for " + payload.getClass().getName());
        }
        return channel;
    }

    private static FriendlyByteBuf write(Object payload) {
        BiConsumer<Object, FriendlyByteBuf> encoder = ENCODERS.get(payload.getClass());
        if (encoder == null) {
            throw new IllegalArgumentException(
                    "No Better Horses encoder registered for " + payload.getClass().getName());
        }
        FriendlyByteBuf buf = PacketByteBufs.create();
        encoder.accept(payload, buf);
        return buf;
    }
}
