package icy.betterhorses.net;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum BhAbility {

    WAR_STEADY(BreedArchetype.WAR),
    WAR_MEDKIT(BreedArchetype.WAR),
    DRAFT_MASS(BreedArchetype.DRAFT),
    DRAFT_HAUL(BreedArchetype.DRAFT),
    WESTERN_ROAD(BreedArchetype.WESTERN),
    PONY_STEP(BreedArchetype.PONY),
    PONY_FALL(BreedArchetype.PONY),
    PONY_SNOW(BreedArchetype.PONY),
    PONY_HEAL(BreedArchetype.PONY),

    THOROUGHBRED_TOP_END(HorseBreed.THOROUGHBRED),
    ARABIAN_ENDURANCE(HorseBreed.ARABIAN),
    QUARTER_BURST(HorseBreed.QUARTER),
    FRIESIAN_CALM(HorseBreed.FRIESIAN),
    FRIESIAN_DRESSAGE(HorseBreed.FRIESIAN),
    ANDALUSIAN_GUARD(HorseBreed.ANDALUSIAN),
    ANDALUSIAN_SAVE(HorseBreed.ANDALUSIAN),
    PERCHERON_MOMENTUM(HorseBreed.PERCHERON),
    PERCHERON_CHAIN(HorseBreed.PERCHERON),
    CLYDESDALE_ARMOR(HorseBreed.CLYDESDALE),
    CLYDESDALE_RESIST(HorseBreed.CLYDESDALE),
    CLYDESDALE_DEFLECT(HorseBreed.CLYDESDALE),
    SHIRE_INTIMIDATE(HorseBreed.SHIRE),
    SHIRE_BRICK(HorseBreed.SHIRE, false),
    BELGIAN_BRICK(HorseBreed.BELGIAN, false),
    ICELANDIC_MOMENTUM(HorseBreed.ICELANDIC),
    ICELANDIC_FREEZE(HorseBreed.ICELANDIC),
    ICELANDIC_CLEAR(HorseBreed.ICELANDIC),
    MUSTANG_ALERT(HorseBreed.MUSTANG),
    MUSTANG_SELF_HEAL(HorseBreed.MUSTANG),
    MUSTANG_RIDER_HEAL(HorseBreed.MUSTANG),
    HAFLINGER_HAUL(HorseBreed.HAFLINGER),
    HAFLINGER_LIGHT(HorseBreed.HAFLINGER),
    MORGAN_BOND(HorseBreed.MORGAN),
    MORGAN_ADAPT(HorseBreed.MORGAN),
    PAINT_SCAN(HorseBreed.AMERICAN_PAINT),
    PAINT_STRUCTURES(HorseBreed.AMERICAN_PAINT),
    APPALOOSA_NIGHT(HorseBreed.APPALOOSA),
    APPALOOSA_HERD(HorseBreed.APPALOOSA),
    APPALOOSA_FEED(HorseBreed.APPALOOSA);

    private final @Nullable BreedArchetype archetype;
    private final @Nullable HorseBreed breed;
    private final boolean fresh;

    BhAbility(BreedArchetype archetype) {
        this.archetype = archetype;
        this.breed = null;
        this.fresh = true;
    }

    BhAbility(HorseBreed breed) {
        this(breed, true);
    }

    BhAbility(HorseBreed breed, boolean fresh) {
        this.archetype = null;
        this.breed = breed;
        this.fresh = fresh;
    }

    public boolean fresh() {
        return this.fresh;
    }

    public boolean classPerk() {
        return this.archetype != null;
    }

    public @Nullable BreedArchetype archetype() {
        return this.archetype;
    }

    public @Nullable HorseBreed breed() {
        return this.breed;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean on() {
        return BhConfig.abilityEnabled(this);
    }
}
