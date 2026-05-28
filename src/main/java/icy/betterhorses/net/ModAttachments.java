package icy.betterhorses.net;

import net.minecraftforge.eventbus.api.IEventBus;

public final class ModAttachments {
    public static final class BhHorseSyncState {
        public int bond = 0;
        public int stabilizerStateId = HorseStabilizerState.CLOSED.ordinal();
        public int gearFlags = 0;
        public int genderId = HorseGender.MALE.ordinal();
        public int breedId = HorseBreed.UNKNOWN_SPECIES.ordinal();
        public boolean breedMixed = false;
    }

    public static void register(IEventBus modEventBus) {
        // Forge 1.20.1 does not have NeoForge attachments. Horse sync now lives on
        // vanilla synched entity data, so this stays as a compatibility no-op.
    }

    private ModAttachments() {}
}
