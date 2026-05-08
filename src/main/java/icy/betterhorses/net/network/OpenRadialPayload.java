package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenRadialPayload(int horseId) implements CustomPacketPayload {

    public static final Type<OpenRadialPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "open_radial"));

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    @Override
    public Type<OpenRadialPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, OpenRadialPayload> {
        @Override
        public OpenRadialPayload decode(FriendlyByteBuf buf) {
            return new OpenRadialPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, OpenRadialPayload value) {
            buf.writeVarInt(value.horseId());
        }
    }
}
