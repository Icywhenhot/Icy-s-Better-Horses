package icy.betterhorses.net.network;

import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;
import java.util.UUID;

public record HorseRosterEntry(
        UUID horseId,
        String customName,
        String breedId,
        String genderId,
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

    /** Legacy ordinal constructor retained for compatibility with older callers. */
    public HorseRosterEntry(UUID horseId, String customName, String breedId, int genderOrdinal,
                            boolean mixedBreed, int bond, boolean loaded, boolean hasHome, boolean active,
                            String dimensionId, BlockPos pos, String entityTypeId, int variantOrdinal,
                            int markingsOrdinal, boolean baby, int breedCoat) {
        this(horseId, customName, breedId,
                IcysBetterHorses.MOD_ID + ":" + HorseGender.fromId(genderOrdinal).name().toLowerCase(Locale.ROOT),
                mixedBreed, bond, loaded, hasHome, active, dimensionId, pos, entityTypeId,
                variantOrdinal, markingsOrdinal, baby, breedCoat);
    }

    /** Legacy ordinal accessor for callers that still consume the old gender representation. */
    public int genderOrdinal() {
        String path = genderId;
        int colon = path.indexOf(':');
        if (colon >= 0) path = path.substring(colon + 1);
        return "female".equals(path) ? HorseGender.FEMALE.ordinal() : HorseGender.MALE.ordinal();
    }

    public static void encode(FriendlyByteBuf buf, HorseRosterEntry entry) {
        buf.writeUUID(entry.horseId());
        buf.writeUtf(entry.customName());
        buf.writeUtf(entry.breedId());
        buf.writeUtf(entry.genderId());
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
                buf.readUUID(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readVarInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readUtf(), buf.readBlockPos(),
                buf.readUtf(), buf.readVarInt() - 1, buf.readVarInt() - 1, buf.readBoolean(), buf.readVarInt() - 1);
    }
}
