package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record CallHorsePayload() {

    public static void encode(CallHorsePayload payload, FriendlyByteBuf buf) {
    }

    public static CallHorsePayload decode(FriendlyByteBuf buf) {
        return new CallHorsePayload();
    }
}
