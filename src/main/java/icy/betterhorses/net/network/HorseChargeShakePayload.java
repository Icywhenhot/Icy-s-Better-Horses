package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record HorseChargeShakePayload() {

    public static void encode(HorseChargeShakePayload payload, FriendlyByteBuf buf) {
    }

    public static HorseChargeShakePayload decode(FriendlyByteBuf buf) {
        return new HorseChargeShakePayload();
    }
}
