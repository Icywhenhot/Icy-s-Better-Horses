package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BhSteerModePayload(int horseId, boolean freeSteer) implements CustomPacketPayload {

    public static final Type<BhSteerModePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "steer_mode"));

    @Override
    public Type<BhSteerModePayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BhSteerModePayload> {
        @Override
        public BhSteerModePayload decode(FriendlyByteBuf buf) {
            return new BhSteerModePayload(buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BhSteerModePayload value) {
            buf.writeVarInt(value.horseId());
            buf.writeBoolean(value.freeSteer());
        }
    }
}
