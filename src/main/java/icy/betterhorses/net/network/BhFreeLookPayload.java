package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BhFreeLookPayload(int horseId, boolean freeLook) implements CustomPacketPayload {

    public static final Type<BhFreeLookPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "free_look"));

    @Override
    public Type<BhFreeLookPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BhFreeLookPayload> {
        @Override
        public BhFreeLookPayload decode(FriendlyByteBuf buf) {
            return new BhFreeLookPayload(buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BhFreeLookPayload value) {
            buf.writeVarInt(value.horseId());
            buf.writeBoolean(value.freeLook());
        }
    }
}
