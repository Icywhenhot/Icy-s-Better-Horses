package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(int toggles, int masters, long abilities) implements CustomPacketPayload {

    public static final Type<ConfigSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "config_sync"));

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, ConfigSyncPayload> {
        @Override
        public ConfigSyncPayload decode(FriendlyByteBuf buf) {
            return new ConfigSyncPayload(buf.readVarInt(), buf.readVarInt(), buf.readLong());
        }

        @Override
        public void encode(FriendlyByteBuf buf, ConfigSyncPayload value) {
            buf.writeVarInt(value.toggles());
            buf.writeVarInt(value.masters());
            buf.writeLong(value.abilities());
        }
    }
}
