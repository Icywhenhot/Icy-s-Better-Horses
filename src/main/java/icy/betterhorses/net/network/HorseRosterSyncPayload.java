package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

public record HorseRosterSyncPayload(List<HorseRosterEntry> entries) implements CustomPacketPayload {

    public static final Type<HorseRosterSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_roster_sync"));

    @Override
    public Type<HorseRosterSyncPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseRosterSyncPayload> {
        private static final net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, List<HorseRosterEntry>> ENTRIES =
                net.minecraft.network.codec.StreamCodec.<FriendlyByteBuf, HorseRosterEntry>of(
                        HorseRosterEntry::encode, HorseRosterEntry::decode).apply(ByteBufCodecs.list());

        @Override
        public HorseRosterSyncPayload decode(FriendlyByteBuf buf) {
            return new HorseRosterSyncPayload(ENTRIES.decode(buf));
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseRosterSyncPayload value) {
            ENTRIES.encode(buf, value.entries());
        }
    }
}
