package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RadialCommandPayload(int horseId, String commandId, String abilityId) implements CustomPacketPayload {

    public static final Type<RadialCommandPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "radial_cmd"));

    public static final StreamCodec STREAM_CODEC = new StreamCodec();

    public RadialCommandPayload(int horseId, String commandId) {
        this(horseId, commandId, "");
    }

    @Override
    public Type<RadialCommandPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, RadialCommandPayload> {
        @Override
        public RadialCommandPayload decode(FriendlyByteBuf buf) {
            return new RadialCommandPayload(buf.readVarInt(), buf.readUtf(), buf.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buf, RadialCommandPayload value) {
            buf.writeVarInt(value.horseId());
            buf.writeUtf(value.commandId());
            buf.writeUtf(value.abilityId());
        }
    }
}
