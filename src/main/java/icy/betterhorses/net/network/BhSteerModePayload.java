package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Tells the server whether the rider is currently in third person, where the camera is free and
 * the horse is steered with A/D rather than pointed with the view.
 *
 * <p>This has to travel. Camera perspective is knowledge only the riding client has, but
 * {@code AbstractHorse.getRiddenRotation} runs on both sides - leave the server ignorant and it
 * keeps snapping the horse back onto the rider's view every tick while the client insists
 * otherwise, which reads in game as the horse fighting the mouse.
 *
 * <p>Sent only on change, not per tick: see {@code HorseSteerModeController}.
 */
public record BhSteerModePayload(int horseId, boolean freeSteer) implements CustomPacketPayload {

    public static final Type<BhSteerModePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("icys-better-horses", "steer_mode"));

    @Override
    public Type<BhSteerModePayload> type() {
        return TYPE;
    }

    public static class StreamCodec
            implements net.minecraft.network.codec.StreamCodec<FriendlyByteBuf, BhSteerModePayload> {
        @Override
        public BhSteerModePayload decode(FriendlyByteBuf buf) {
            return new BhSteerModePayload(buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, BhSteerModePayload value) {
            buf.writeVarInt(value.horseId());
            buf.writeBoolean(value.freeSteer());
        }
    }
}
