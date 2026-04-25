package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RadialCommandPayload(int horseId, int commandOrdinal) implements CustomPacketPayload {

    public static final Type<RadialCommandPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "radial_cmd"));

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    @Override
    public Type<RadialCommandPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RadialCommandPayload> {
        @Override
        public RadialCommandPayload decode(FriendlyByteBuf buf) {
            return new RadialCommandPayload(buf.readVarInt(), buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, RadialCommandPayload value) {
            buf.writeVarInt(value.horseId());
            buf.writeVarInt(value.commandOrdinal());
        }
    }
}
