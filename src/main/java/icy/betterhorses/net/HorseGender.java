package icy.betterhorses.net;

import net.minecraft.network.chat.Component;

public enum HorseGender {
    MALE,
    FEMALE;

    private static final HorseGender[] VALUES = values();

    public static HorseGender fromId(int id) {
        if (id < 0 || id >= VALUES.length) return MALE;
        return VALUES[id];
    }

    public Component displayName() {
        return Component.translatable("gender.icys-better-horses." + name().toLowerCase());
    }
}
