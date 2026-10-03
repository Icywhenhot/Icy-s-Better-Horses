package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HorseJumpPayload(int horseId) implements CustomPacketPayload {

    public static final Type<HorseJumpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "horse_jump"));

    @Override
    public Type<HorseJumpPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseJumpPayload> {
        @Override
        public HorseJumpPayload decode(FriendlyByteBuf buf) {
            return new HorseJumpPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseJumpPayload value) {
            buf.writeVarInt(value.horseId());
        }
    }
}
