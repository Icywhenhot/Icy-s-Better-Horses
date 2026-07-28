package icy.betterhorses.net.network;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * One row of the horse management screen. Built server-side from the live entity when the horse is
 * loaded, and from its stored whistle snapshot when it isn't — so horses resting in unloaded chunks
 * still show up.
 *
 * <p>Breed and gender travel as ordinals rather than pre-rendered text so the client resolves the
 * labels in its own language.</p>
 */
public record HorseRosterEntry(
        UUID horseId,
        /** Custom name, or empty when the horse was never named. */
        String customName,
        int breedOrdinal,
        int genderOrdinal,
        boolean mixedBreed,
        int bond,
        boolean loaded,
        boolean hasHome,
        /** True for the horse the whistle keybind currently calls. */
        boolean active,
        /** Identifier of the dimension the horse was last seen in, e.g. {@code minecraft:overworld}. */
        String dimensionId,
        // Everything below exists only so the client can rebuild a stand-in horse for the preview pane.
        String entityTypeId,
        /** {@code Variant} ordinal (coat colour), or -1 for equines that don't have one. */
        int variantOrdinal,
        /** {@code Markings} ordinal, or -1. */
        int markingsOrdinal,
        boolean baby) {

    public static void encode(FriendlyByteBuf buf, HorseRosterEntry entry) {
        buf.writeUUID(entry.horseId());
        buf.writeUtf(entry.customName());
        buf.writeVarInt(entry.breedOrdinal());
        buf.writeVarInt(entry.genderOrdinal());
        buf.writeBoolean(entry.mixedBreed());
        buf.writeVarInt(entry.bond());
        buf.writeBoolean(entry.loaded());
        buf.writeBoolean(entry.hasHome());
        buf.writeBoolean(entry.active());
        buf.writeUtf(entry.dimensionId());
        buf.writeUtf(entry.entityTypeId());
        // +1 so the "absent" sentinel survives writeVarInt, which can't carry -1 cheaply.
        buf.writeVarInt(entry.variantOrdinal() + 1);
        buf.writeVarInt(entry.markingsOrdinal() + 1);
        buf.writeBoolean(entry.baby());
    }

    public static HorseRosterEntry decode(FriendlyByteBuf buf) {
        return new HorseRosterEntry(
                buf.readUUID(),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readVarInt() - 1,
                buf.readVarInt() - 1,
                buf.readBoolean());
    }
}
