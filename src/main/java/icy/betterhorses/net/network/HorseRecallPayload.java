package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record HorseRecallPayload() {

    public static void encode(HorseRecallPayload payload, FriendlyByteBuf buf) {
    }

    public static HorseRecallPayload decode(FriendlyByteBuf buf) {
        return new HorseRecallPayload();
    }
}
