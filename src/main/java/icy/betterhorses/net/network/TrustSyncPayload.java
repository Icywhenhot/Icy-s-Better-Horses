package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import java.util.List;
import java.util.UUID;

public record TrustSyncPayload(List<UUID> trustingOwners) {

    public static void encode(TrustSyncPayload payload, FriendlyByteBuf buf) {
            buf.writeCollection(payload.trustingOwners(), (b, id) -> ((FriendlyByteBuf) b).writeUUID(id));
    }

    public static TrustSyncPayload decode(FriendlyByteBuf buf) {
            return new TrustSyncPayload(buf.readList(b -> ((FriendlyByteBuf) b).readUUID()));
    }
}
