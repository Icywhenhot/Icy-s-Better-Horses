package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record HorseGearPayload(int horseId, int gear, int gaitGear) {

    public static void encode(HorseGearPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.horseId());
            buf.writeVarInt(payload.gear());
            buf.writeVarInt(payload.gaitGear());
    }

    public static HorseGearPayload decode(FriendlyByteBuf buf) {
            return new HorseGearPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }
}
