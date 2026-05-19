package icy.betterhorses.net;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;

/**
 * Horse breed identifier. The first 15 entries are real horse breeds eligible
 * for the breeding system. The trailing entries are species placeholders used
 * to label non-horse AbstractHorse subclasses on the info screen.
 */
public enum HorseBreed {
    THOROUGHBRED,
    ARABIAN,
    QUARTER,
    FRIESIAN,
    ANDALUSIAN,
    PERCHERON,
    CLYDESDALE,
    SHIRE,
    BELGIAN,
    ICELANDIC,
    MUSTANG,
    HAFLINGER,
    MORGAN,
    AMERICAN_PAINT,
    APPALOOSA,

    // Species placeholders (not selectable for breeding rolls).
    DONKEY_SPECIES,
    MULE_SPECIES,
    SKELETON_SPECIES,
    ZOMBIE_SPECIES,
    UNKNOWN_SPECIES;

    private static final HorseBreed[] VALUES = values();
    public static final int HORSE_BREED_COUNT = 15;

    public static HorseBreed fromId(int id) {
        if (id < 0 || id >= VALUES.length) return UNKNOWN_SPECIES;
        return VALUES[id];
    }

    public boolean isRealBreed() {
        return ordinal() < HORSE_BREED_COUNT;
    }

    public Component displayName() {
        return Component.translatable("breed.icys-better-horses." + name().toLowerCase());
    }

    public Component displayName(boolean mixed) {
        if (!mixed || !isRealBreed()) {
            return displayName();
        }
        return Component.translatable(
                "breed.icys-better-horses.mix_format",
                Component.translatable("breed.icys-better-horses." + name().toLowerCase()));
    }

    /** Returns the appropriate placeholder breed for non-horse AbstractHorse subclasses, or null if it's a real horse. */
    public static HorseBreed speciesFor(AbstractHorse horse) {
        if (horse instanceof Horse) return null;
        if (horse instanceof Donkey) return DONKEY_SPECIES;
        if (horse instanceof Mule) return MULE_SPECIES;
        if (horse instanceof SkeletonHorse) return SKELETON_SPECIES;
        if (horse instanceof ZombieHorse) return ZOMBIE_SPECIES;
        return UNKNOWN_SPECIES;
    }
}
