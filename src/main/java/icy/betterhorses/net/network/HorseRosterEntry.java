package icy.betterhorses.net.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

// one row of the horse management screen
public record HorseRosterEntry(
        UUID horseId,
        // custom name, or empty when the horse was never named
        String customName,
        int breedOrdinal,
        int genderOrdinal,
        boolean mixedBreed,
        int bond,
        boolean loaded,
        boolean hasHome,
        // true for the horse the whistle keybind currently calls
        boolean active,
        // identifier of the dimension the horse was last seen in, e.g
        String dimensionId,
        // where the horse is standing, or last stood before its chunk unloaded
        BlockPos pos,
        // everything below exists only so the client can rebuild a stand-in horse for the preview pane
        String entityTypeId,
        // variant ordinal (coat colour), or -1 for equines that don't have one
        int variantOrdinal,
        // markings ordinal, or -1
        int markingsOrdinal,
        boolean baby,
        // breed-coat index for dedicated breed mobs, or -1 for plain equines. Without this the
        // preview pane rebuilds a stand-in horse whose coat defaults to 0, so every Icelandic
        // showed up black regardless of its real coat.
        int breedCoat) {

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
        buf.writeBlockPos(entry.pos());
        buf.writeUtf(entry.entityTypeId());
        // +1 so the "absent" sentinel survives writeVarInt, which can't carry -1 cheaply
        buf.writeVarInt(entry.variantOrdinal() + 1);
        buf.writeVarInt(entry.markingsOrdinal() + 1);
        buf.writeBoolean(entry.baby());
        buf.writeVarInt(entry.breedCoat() + 1);
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
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readVarInt() - 1,
                buf.readVarInt() - 1,
                buf.readBoolean(),
                buf.readVarInt() - 1);
    }
}
