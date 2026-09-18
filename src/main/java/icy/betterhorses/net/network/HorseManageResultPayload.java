package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;

public record HorseManageResultPayload(UUID horseId, int actionOrdinal, boolean success, String messageKey) {

    public static void encode(HorseManageResultPayload payload, FriendlyByteBuf buf) {
            buf.writeUUID(payload.horseId());
            buf.writeVarInt(payload.actionOrdinal());
            buf.writeBoolean(payload.success());
            buf.writeUtf(payload.messageKey());
    }

    public static HorseManageResultPayload decode(FriendlyByteBuf buf) {
            return new HorseManageResultPayload(buf.readUUID(), buf.readVarInt(), buf.readBoolean(), buf.readUtf());
    }
}
