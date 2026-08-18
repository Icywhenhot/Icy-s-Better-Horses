package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HorseGearPayload(int horseId, int gear, int gaitGear) implements CustomPacketPayload {

    public static final Type<HorseGearPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_gear"));

    @Override
    public Type<HorseGearPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseGearPayload> {
        @Override
        public HorseGearPayload decode(FriendlyByteBuf buf) {
            return new HorseGearPayload(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseGearPayload value) {
            buf.writeVarInt(value.horseId());
            buf.writeVarInt(value.gear());
            buf.writeVarInt(value.gaitGear());
        }
    }
}
