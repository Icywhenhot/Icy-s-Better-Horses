package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Server → client: the outcome of a management action. The screen uses it to flash the button that
 * was pressed red and show {@code messageKey} next to it; on success it just clears any old flash.
 */
public record HorseManageResultPayload(UUID horseId, int actionOrdinal, boolean success, String messageKey)
        implements CustomPacketPayload {

    public static final Type<HorseManageResultPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_manage_result"));

    @Override
    public Type<HorseManageResultPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseManageResultPayload> {
        @Override
        public HorseManageResultPayload decode(FriendlyByteBuf buf) {
            return new HorseManageResultPayload(buf.readUUID(), buf.readVarInt(), buf.readBoolean(), buf.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseManageResultPayload value) {
            buf.writeUUID(value.horseId());
            buf.writeVarInt(value.actionOrdinal());
            buf.writeBoolean(value.success());
            buf.writeUtf(value.messageKey());
        }
    }
}
