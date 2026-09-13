package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CartSizePayload(int targetId) implements CustomPacketPayload {

    public static final Type<CartSizePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "cart_size"));

    @Override
    public Type<CartSizePayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CartSizePayload> {
        @Override
        public CartSizePayload decode(FriendlyByteBuf buf) {
            return new CartSizePayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, CartSizePayload value) {
            buf.writeVarInt(value.targetId());
        }
    }
}
