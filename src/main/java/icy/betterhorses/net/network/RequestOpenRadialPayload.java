package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record RequestOpenRadialPayload(int horseId) {

    public static void encode(RequestOpenRadialPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.horseId());
    }

    public static RequestOpenRadialPayload decode(FriendlyByteBuf buf) {
        return new RequestOpenRadialPayload(buf.readVarInt());
    }
}
