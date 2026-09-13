package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HorseChargeShakePayload() implements CustomPacketPayload {

    public static final Type<HorseChargeShakePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_charge_shake"));

    @Override
    public Type<HorseChargeShakePayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseChargeShakePayload> {
        @Override
        public HorseChargeShakePayload decode(FriendlyByteBuf buf) {
            return new HorseChargeShakePayload();
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseChargeShakePayload value) {
        }
    }
}
