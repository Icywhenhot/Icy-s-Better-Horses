package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// client → server: "send me my horse roster" (screen opened, or refreshed after an action)
public record OpenHorseRosterPayload() implements CustomPacketPayload {

    public static final Type<OpenHorseRosterPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "open_horse_roster"));

    @Override
    public Type<OpenHorseRosterPayload> type() {
        return TYPE;
    }

    public static class StreamCodec implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, OpenHorseRosterPayload> {
        @Override
        public OpenHorseRosterPayload decode(FriendlyByteBuf buf) {
            return new OpenHorseRosterPayload();
        }

        @Override
        public void encode(FriendlyByteBuf buf, OpenHorseRosterPayload value) {
            // no data
        }
    }
}
