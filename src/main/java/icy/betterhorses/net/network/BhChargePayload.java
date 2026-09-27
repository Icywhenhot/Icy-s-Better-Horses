package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BhChargePayload(boolean on) implements CustomPacketPayload {

    public static final Type<BhChargePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "charge_toggle"));

    @Override
    public Type<BhChargePayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BhChargePayload> {
        @Override
        public BhChargePayload decode(FriendlyByteBuf buf) {
            return new BhChargePayload(buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BhChargePayload value) {
            buf.writeBoolean(value.on());
        }
    }
}
