package icy.betterhorses.net;

public enum HorseSpecies {
    NONE, DONKEY, MULE, SKELETON, ZOMBIE;

    private static final HorseSpecies[] VALUES = values();

    public static HorseSpecies fromId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : NONE;
    }
}
