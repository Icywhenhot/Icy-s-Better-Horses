package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.UUID;

public record TrustSyncPayload(List<UUID> trustingOwners) implements CustomPacketPayload {

    public static final Type<TrustSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "trust_sync"));

    @Override
    public Type<TrustSyncPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, TrustSyncPayload> {
        @Override
        public TrustSyncPayload decode(FriendlyByteBuf buf) {
            return new TrustSyncPayload(buf.readList(b -> ((FriendlyByteBuf) b).readUUID()));
        }

        @Override
        public void encode(FriendlyByteBuf buf, TrustSyncPayload value) {
            buf.writeCollection(value.trustingOwners(), (b, id) -> ((FriendlyByteBuf) b).writeUUID(id));
        }
    }
}
