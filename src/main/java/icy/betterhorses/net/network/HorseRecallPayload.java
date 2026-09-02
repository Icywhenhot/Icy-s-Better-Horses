package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HorseRecallPayload() implements CustomPacketPayload {

    public static final Type<HorseRecallPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_recall"));

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    @Override
    public Type<HorseRecallPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseRecallPayload> {
        @Override
        public HorseRecallPayload decode(FriendlyByteBuf buf) {
            return new HorseRecallPayload();
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseRecallPayload value) {
        }
    }
}
