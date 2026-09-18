package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record BhFreeLookPayload(int horseId, boolean freeLook) {

    public static void encode(BhFreeLookPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.horseId());
            buf.writeBoolean(payload.freeLook());
    }

    public static BhFreeLookPayload decode(FriendlyByteBuf buf) {
            return new BhFreeLookPayload(buf.readVarInt(), buf.readBoolean());
    }
}
