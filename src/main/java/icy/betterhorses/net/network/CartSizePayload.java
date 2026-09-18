package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record CartSizePayload(int targetId) {

    public static void encode(CartSizePayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.targetId());
    }

    public static CartSizePayload decode(FriendlyByteBuf buf) {
            return new CartSizePayload(buf.readVarInt());
    }
}
