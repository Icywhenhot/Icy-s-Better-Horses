package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CallHorsePayload() implements CustomPacketPayload {

    public static final Type<CallHorsePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "call_horse"));

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    @Override
    public Type<CallHorsePayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, CallHorsePayload> {
        @Override
        public CallHorsePayload decode(FriendlyByteBuf buf) {
            return new CallHorsePayload();
        }

        @Override
        public void encode(FriendlyByteBuf buf, CallHorsePayload value) {
            // no data
        }
    }
}
