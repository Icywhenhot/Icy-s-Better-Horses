package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

// server → client: the player's full horse roster, resent after every action that changes
public record HorseRosterSyncPayload(List<HorseRosterEntry> entries) implements CustomPacketPayload {

    public static final Type<HorseRosterSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_roster_sync"));

    @Override
    public Type<HorseRosterSyncPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseRosterSyncPayload> {
        @Override
        public HorseRosterSyncPayload decode(FriendlyByteBuf buf) {
            return new HorseRosterSyncPayload(buf.readList(b -> HorseRosterEntry.decode((FriendlyByteBuf) b)));
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseRosterSyncPayload value) {
            buf.writeCollection(value.entries(), (b, entry) -> HorseRosterEntry.encode((FriendlyByteBuf) b, entry));
        }
    }
}
