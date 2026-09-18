package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

public record HorseRosterSyncPayload(List<HorseRosterEntry> entries) {



    public static void encode(HorseRosterSyncPayload payload, FriendlyByteBuf buf) {
        buf.writeCollection(payload.entries(), (b, entry) -> HorseRosterEntry.encode((FriendlyByteBuf) b, entry));
    }

    public static HorseRosterSyncPayload decode(FriendlyByteBuf buf) {
        return new HorseRosterSyncPayload(buf.readList(b -> HorseRosterEntry.decode((FriendlyByteBuf) b)));
    }
}
