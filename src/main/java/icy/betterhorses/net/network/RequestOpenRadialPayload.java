package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestOpenRadialPayload(int horseId) implements CustomPacketPayload {

    public static final Type<RequestOpenRadialPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "request_open_radial"));

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    @Override
    public Type<RequestOpenRadialPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RequestOpenRadialPayload> {
        @Override
        public RequestOpenRadialPayload decode(FriendlyByteBuf buf) {
            return new RequestOpenRadialPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, RequestOpenRadialPayload value) {
            buf.writeVarInt(value.horseId());
        }
    }
}
