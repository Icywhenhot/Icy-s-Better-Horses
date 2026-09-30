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
    HORSE_COMBAT("horse_combat", true),
    TRANSPARENT_HORSES("transparent_horses"),
    GENDER_BREEDING("gender_breeding"),
    CART_PICKUP("cart_pickup"),
    LEAF_PASSTHROUGH("leaf_passthrough"),
    HOOF_DUST("hoof_dust"),
    REAR_NEIGH("rear_neigh"),
    HORSE_CHARGE("horse_charge", true),
    HORSE_KICK("horse_kick", true),
    HORSE_DEFEND("horse_defend", true),
    HORSE_SPOOK("horse_spook", true),
    HORSE_PVP("horse_pvp", true);

    private static final Map<String, BhFeature> BY_KEY = new HashMap<>();

    static {
        for (BhFeature feature : values()) {
            BY_KEY.put(feature.key, feature);
        }
    }

    private final String key;
    private final boolean combat;

    BhFeature(String key) {
        this(key, false);
    }

    BhFeature(String key, boolean combat) {
        this.key = key;
        this.combat = combat;
    }

    public String key() {
        return key;
    }

    public boolean combat() {
        return combat;
    }

    public boolean on() {
        if (combat && this != HORSE_COMBAT && !BhConfig.horseCombatEnabled()) {
            return false;
        }
        return BhConfig.featureEnabled(this);
    }

    public static @Nullable BhFeature byKey(String key) {
        return BY_KEY.get(key);
    }
}
