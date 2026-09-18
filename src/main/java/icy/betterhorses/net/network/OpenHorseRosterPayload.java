package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenHorseRosterPayload() {

    public static void encode(OpenHorseRosterPayload payload, FriendlyByteBuf buf) {
    }

    public static OpenHorseRosterPayload decode(FriendlyByteBuf buf) {
        return new OpenHorseRosterPayload();
    }
}
