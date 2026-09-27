package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record BhChargePayload(boolean on) {

    public static void encode(BhChargePayload payload, FriendlyByteBuf buf) {
            buf.writeBoolean(payload.on());
    }

    public static BhChargePayload decode(FriendlyByteBuf buf) {
            return new BhChargePayload(buf.readBoolean());
    }
}
