package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

public record HorseManagePayload(UUID horseId, int actionOrdinal) {

    public static void encode(HorseManagePayload payload, FriendlyByteBuf buf) {
            buf.writeUUID(payload.horseId());
            buf.writeVarInt(payload.actionOrdinal());
    }

    public static HorseManagePayload decode(FriendlyByteBuf buf) {
            return new HorseManagePayload(buf.readUUID(), buf.readVarInt());
    }
}
