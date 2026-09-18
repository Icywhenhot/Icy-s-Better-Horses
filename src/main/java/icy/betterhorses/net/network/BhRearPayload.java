package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record BhRearPayload(int horseId) {

    public static void encode(BhRearPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.horseId());
    }

    public static BhRearPayload decode(FriendlyByteBuf buf) {
            return new BhRearPayload(buf.readVarInt());
    }
}
