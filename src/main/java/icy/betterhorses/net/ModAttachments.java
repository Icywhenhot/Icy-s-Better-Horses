package icy.betterhorses.net;

public final class ModAttachments {
    public static final class BhHorseSyncState {
        public int bond = 0;
        public int stabilizerStateId = HorseStabilizerState.CLOSED.ordinal();
        public int gearFlags = 0;
        public int genderId = HorseGender.MALE.ordinal();
        public int breedId = HorseBreed.UNKNOWN_SPECIES.ordinal();
        public boolean breedMixed = false;
    }

    public static void register() {}

    private ModAttachments() {}
}
