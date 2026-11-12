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
    DONKEY_GUARD(BreedArchetype.DONKEY),

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
    PAINT_LOOTING(HorseBreed.AMERICAN_PAINT),
    APPALOOSA_NIGHT(HorseBreed.APPALOOSA),
    APPALOOSA_HERD(HorseBreed.APPALOOSA),
    APPALOOSA_FEED(HorseBreed.APPALOOSA),
    MINIATURE_SKITTISH(HorseBreed.MINIATURE_DONKEY),
    MINIATURE_FARM(HorseBreed.MINIATURE_DONKEY),
    MAMMOTH_SIRE(HorseBreed.MAMMOTH_DONKEY),
    PEGA_SIRE(HorseBreed.PEGA_DONKEY),
    ANDALUSIAN_FARM(HorseBreed.ANDALUSIAN_DONKEY),
    ASINARA_WATER(HorseBreed.ASINARA_DONKEY),
    ASINARA_FISH(HorseBreed.ASINARA_DONKEY),
    ASINARA_TREASURE(HorseBreed.ASINARA_DONKEY),
    MINIATURE_PICKUP(HorseBreed.MINIATURE_DONKEY),
    MINIATURE_BASKET(HorseBreed.MINIATURE_DONKEY),
    MAMMOTH_RUBBLE(HorseBreed.MAMMOTH_DONKEY),
    MAMMOTH_WORK(HorseBreed.MAMMOTH_DONKEY),
    MAMMOTH_BRACE(HorseBreed.MAMMOTH_DONKEY),
    PEGA_SEAT(HorseBreed.PEGA_DONKEY),
    PEGA_SELF_HEAL(HorseBreed.PEGA_DONKEY),
    PEGA_RIDER_HEAL(HorseBreed.PEGA_DONKEY),
    POITOU_HIDE(HorseBreed.POITOU_DONKEY),
    POITOU_CLEAR(HorseBreed.POITOU_DONKEY),
    ZAMORANO_ICE(HorseBreed.ZAMORANO_DONKEY),
    ZAMORANO_SNOW(HorseBreed.ZAMORANO_DONKEY),
    SKELETON_BREATH(HorseBreed.SKELETON_SPECIES),
    SKELETON_DIVE(HorseBreed.SKELETON_SPECIES),
    SKELETON_DASH(HorseBreed.SKELETON_SPECIES),
    ZOMBIE_RATIONS(HorseBreed.ZOMBIE_SPECIES),
    ZOMBIE_FUEL(HorseBreed.ZOMBIE_SPECIES),
    ZOMBIE_ROT(HorseBreed.ZOMBIE_SPECIES),
    STRAY_CHILL(HorseBreed.STRAY_SPECIES),
    SCULK_HUSH(HorseBreed.SCULK_SPECIES),
    SCULK_KIN(HorseBreed.SCULK_SPECIES),
    SCULK_LULL(HorseBreed.SCULK_SPECIES),
    HUSK_THIRST(HorseBreed.HUSK_SPECIES),
    DROWNED_BREATH(HorseBreed.DROWNED_SPECIES),
    DROWNED_GILLS(HorseBreed.DROWNED_SPECIES),
    DROWNED_SWIM(HorseBreed.DROWNED_SPECIES);

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

    public boolean appliesTo(HorseBreed breed) {
        return this.breed == breed
                || this.breed == HorseBreed.SKELETON_SPECIES && breed.isSkeleton()
                || this.breed == HorseBreed.ZOMBIE_SPECIES && breed.isZombie()
                || this.breed != null && this.breed.sameUndeadVariant(breed);
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean on() {
        return BhConfig.abilityEnabled(this);
    }
}
