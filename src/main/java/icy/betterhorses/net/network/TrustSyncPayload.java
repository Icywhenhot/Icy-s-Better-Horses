package icy.betterhorses.net.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
        private static final net.minecraft.network.codec.StreamCodec<ByteBuf, List<UUID>> OWNERS =
                UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list());

        @Override
        public TrustSyncPayload decode(FriendlyByteBuf buf) {
            return new TrustSyncPayload(OWNERS.decode(buf));
        }

        @Override
        public void encode(FriendlyByteBuf buf, TrustSyncPayload value) {
            OWNERS.encode(buf, value.trustingOwners());
        }
    }
}
