package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Asks the server to rear the horse the player is riding, or the tamed one they are looking at.
 *
 * <p>This has to travel rather than being posed locally. Rearing is vanilla entity state - flag 32
 * plus a 20 tick counter, set by {@code standIfPossible} - and the model reads it back out of
 * {@code standAnimation}. Setting it client-side would pose the horse on the presser's screen
 * alone, and the server would keep overwriting the flag with its own value anyway.
 *
 * <p>Carries the horse id because the key works dismounted too: the client has already picked the
 * horse under the crosshair, and re-raycasting server-side would be a second, disagreeing answer.
 * The server still re-checks range and permission - see {@code findCommandHorse}.
 */
public record BhRearPayload(int horseId) implements CustomPacketPayload {

    public static final Type<BhRearPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "rear"));

    @Override
    public Type<BhRearPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BhRearPayload> {
        @Override
        public BhRearPayload decode(FriendlyByteBuf buf) {
            return new BhRearPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BhRearPayload value) {
            buf.writeVarInt(value.horseId());
        }
    }
}
