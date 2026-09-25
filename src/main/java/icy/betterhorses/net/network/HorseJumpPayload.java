package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record HorseJumpPayload(int horseId) {

    public static void encode(HorseJumpPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.horseId());
    }

    public static HorseJumpPayload decode(FriendlyByteBuf buf) {
        return new HorseJumpPayload(buf.readVarInt());
    }
}
