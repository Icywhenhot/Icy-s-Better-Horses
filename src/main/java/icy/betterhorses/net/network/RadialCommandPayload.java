package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record RadialCommandPayload(int horseId, int commandOrdinal) {

    public static void encode(RadialCommandPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.horseId());
        buf.writeVarInt(payload.commandOrdinal());
    }

    public static RadialCommandPayload decode(FriendlyByteBuf buf) {
        return new RadialCommandPayload(buf.readVarInt(), buf.readVarInt());
    }
}
