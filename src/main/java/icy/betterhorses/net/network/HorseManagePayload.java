package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

// client → server: run one of the management actions on a specific owned horse
public record HorseManagePayload(UUID horseId, int actionOrdinal) implements CustomPacketPayload {

    public static final Type<HorseManagePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "horse_manage"));

    @Override
    public Type<HorseManagePayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, HorseManagePayload> {
        @Override
        public HorseManagePayload decode(FriendlyByteBuf buf) {
            return new HorseManagePayload(buf.readUUID(), buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, HorseManagePayload value) {
            buf.writeUUID(value.horseId());
            buf.writeVarInt(value.actionOrdinal());
        }
    }
}
