package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CartMenuPayload(int targetId) implements CustomPacketPayload {

    public static final Type<CartMenuPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "cart_menu"));

    @Override
    public Type<CartMenuPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CartMenuPayload> {
        @Override
        public CartMenuPayload decode(FriendlyByteBuf buf) {
            return new CartMenuPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, CartMenuPayload value) {
            buf.writeVarInt(value.targetId());
        }
    }
}
