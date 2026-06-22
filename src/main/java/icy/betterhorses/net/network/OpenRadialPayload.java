package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenRadialPayload(int horseId) {

    public static void encode(OpenRadialPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.horseId());
    }

    public static OpenRadialPayload decode(FriendlyByteBuf buf) {
        return new OpenRadialPayload(buf.readVarInt());
    }
}
