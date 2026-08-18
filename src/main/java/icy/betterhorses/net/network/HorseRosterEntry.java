package icy.betterhorses.net.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record HorseRosterEntry(
        UUID horseId,
        String customName,
        int breedOrdinal,
        int genderOrdinal,
        boolean mixedBreed,
        int bond,
        boolean loaded,
        boolean hasHome,
        boolean active,
        String dimensionId,
        BlockPos pos,
        String entityTypeId,
        int variantOrdinal,
        int markingsOrdinal,
        boolean baby,
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
