package icy.betterhorses.net;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;

import java.util.Objects;

public enum HorseCommand {
    FOLLOW,
    STAY,
    RETURN_HOME,
    SET_HOME,
    WANDER,
    ABILITY;

    public static boolean toggleable(ResourceKey<BreedType> breedKey) {
        return Objects.equals(breedKey, BhContent.APPALOOSA.getKey());
    }

    public static HorseCommand fromId(int id) {
        HorseCommand[] values = values();
        return values[Math.max(0, Math.min(id, values.length - 1))];
    }
}
