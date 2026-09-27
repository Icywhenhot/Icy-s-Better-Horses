package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RiderPanelPayload(boolean shown) implements CustomPacketPayload {

    public static final Type<RiderPanelPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("icys-better-horses", "rider_panel"));

    @Override
    public Type<RiderPanelPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RiderPanelPayload> {
        @Override
        public RiderPanelPayload decode(FriendlyByteBuf buf) {
            return new RiderPanelPayload(buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, RiderPanelPayload value) {
            buf.writeBoolean(value.shown());
        }
    }
}
