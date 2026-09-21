package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record RadialCommandPayload(int horseId, String commandId) {

    public static void encode(RadialCommandPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.horseId());
            buf.writeUtf(payload.commandId());
    }

    public static RadialCommandPayload decode(FriendlyByteBuf buf) {
            return new RadialCommandPayload(buf.readVarInt(), buf.readUtf());
    }
}
