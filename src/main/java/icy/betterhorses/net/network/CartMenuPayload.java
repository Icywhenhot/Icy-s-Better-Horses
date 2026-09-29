package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record CartMenuPayload(int targetId) {

    public static void encode(CartMenuPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.targetId());
    }

    public static CartMenuPayload decode(FriendlyByteBuf buf) {
            return new CartMenuPayload(buf.readVarInt());
    }
}
