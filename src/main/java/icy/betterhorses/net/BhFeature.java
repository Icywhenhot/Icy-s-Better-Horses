package icy.betterhorses.net;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum BhFeature {

    STABILIZER("stabilizer"),
    MEDKIT("medkit"),
    HOOVES("hooves"),
    HORSE_EXCLUSIVITY("horse_exclusivity"),
    MULTI_RIDING("multiriding"),
    HORSE_COMBAT("horse_combat"),
    TRANSPARENT_HORSES("transparent_horses"),
    GENDER_BREEDING("gender_breeding"),
    HORSE_PVP("horse_pvp"),
    CART_PICKUP("cart_pickup"),
    CONVERT_TAMED_HORSES("convert_tamed_horses");

    private static final Map<String, BhFeature> BY_KEY = new HashMap<>();

    static {
        for (BhFeature feature : values()) {
            BY_KEY.put(feature.key, feature);
        }
    }

    private final String key;

    BhFeature(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public boolean on() {
        return BhConfig.featureEnabled(this);
    }

    public static @Nullable BhFeature byKey(String key) {
        return BY_KEY.get(key);
    }
}
