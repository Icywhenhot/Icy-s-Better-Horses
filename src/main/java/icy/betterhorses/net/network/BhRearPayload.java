package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BhRearPayload(int horseId) implements CustomPacketPayload {

    public static final Type<BhRearPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "rear"));

    @Override
    public Type<BhRearPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BhRearPayload> {
        @Override
        public BhRearPayload decode(FriendlyByteBuf buf) {
            return new BhRearPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BhRearPayload value) {
            buf.writeVarInt(value.horseId());
        }
    }
}
