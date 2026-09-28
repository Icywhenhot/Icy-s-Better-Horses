package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

public record RiderPanelPayload(boolean shown) {

    public static void encode(RiderPanelPayload payload, FriendlyByteBuf buf) {
        buf.writeBoolean(payload.shown());
    }

    public static RiderPanelPayload decode(FriendlyByteBuf buf) {
        return new RiderPanelPayload(buf.readBoolean());
    }
}
