package icy.betterhorses.net;

import icy.betterhorses.net.network.BhFreeLookPayload;
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseJumpPayload;
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
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.CommandType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
        channel(RadialCommandPayload.class, "radial_command", RadialCommandPayload::encode);
        channel(CallHorsePayload.class, "call_horse", CallHorsePayload::encode);
        channel(HorseRecallPayload.class, "horse_recall", HorseRecallPayload::encode);
        channel(OpenHorseRosterPayload.class, "open_horse_roster", OpenHorseRosterPayload::encode);
        channel(HorseManagePayload.class, "horse_manage", HorseManagePayload::encode);
        channel(HorseGearPayload.class, "horse_gear", HorseGearPayload::encode);
        channel(BhFreeLookPayload.class, "free_look", BhFreeLookPayload::encode);
        channel(BhRearPayload.class, "rear", BhRearPayload::encode);
        channel(CartSizePayload.class, "cart_size", CartSizePayload::encode);

        channel(HorseRosterSyncPayload.class, "horse_roster_sync", HorseRosterSyncPayload::encode);
        channel(HorseManageResultPayload.class, "horse_manage_result", HorseManageResultPayload::encode);
        channel(TrustSyncPayload.class, "trust_sync", TrustSyncPayload::encode);
        channel(HorseChargeShakePayload.class, "charge_shake", HorseChargeShakePayload::encode);
        channel(HorseJumpPayload.class, "horse_jump", HorseJumpPayload::encode);
        channel(ConfigSyncPayload.class, "config_sync", ConfigSyncPayload::encode);
        channel(BreedDataPayload.class, "breed_data", BreedDataPayload::encode);
    }

    public static void registerServer() {
        toServer(RadialCommandPayload.class, RadialCommandPayload::decode,
                (payload, player) -> IcysBetterHorses.handleRadialCommand(
                        player, payload.horseId(), bh_parseCommand(payload.commandId()), payload.abilityId()));
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
        toClient(HorseJumpPayload.class, HorseJumpPayload::decode,
                IcysBetterHorsesClient::receiveJump);
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

    private static @Nullable ResourceKey<CommandType> bh_parseCommand(String raw) {
        ResourceLocation loc = ResourceLocation.tryParse(raw);
        return loc != null
                ? ResourceKey.create(BhRegistries.COMMAND_TYPES, loc)
                : null;
    }

    public static void sendToServer(Object payload) {
        ClientPlayNetworking.send(channelOf(payload), write(payload));
    }

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

    public static void sendToTracking(Entity entity, Object payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendToPlayer(player, payload);
        }
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
