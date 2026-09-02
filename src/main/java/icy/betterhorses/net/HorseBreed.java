package icy.betterhorses.net;

import icy.betterhorses.net.feature.breed.BreedAbility;
import icy.betterhorses.net.feature.breed.BrickBreak;
import icy.betterhorses.net.feature.breed.EasyKeeper;
import icy.betterhorses.net.feature.breed.HardyNorthern;
import icy.betterhorses.net.feature.breed.Hearthlight;
import icy.betterhorses.net.feature.breed.Intimidation;
import icy.betterhorses.net.feature.breed.Ironclad;
import icy.betterhorses.net.feature.breed.SecondChance;
import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import icy.betterhorses.net.feature.breed.StockHorse;
import icy.betterhorses.net.feature.breed.FriesianPresence;
import icy.betterhorses.net.feature.breed.WildInstincts;
import icy.betterhorses.net.feature.breed.Endurance;
import icy.betterhorses.net.feature.breed.StandstillBurst;
import icy.betterhorses.net.feature.breed.TopEnd;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.entity.animal.equine.ZombieHorse;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

public enum HorseBreed {
    THOROUGHBRED(BreedArchetype.RACE, TopEnd::new),
    ARABIAN(BreedArchetype.RACE, Endurance::new),
    QUARTER(BreedArchetype.RACE, StandstillBurst::new),
    FRIESIAN(BreedArchetype.WAR, FriesianPresence::new),
    ANDALUSIAN(BreedArchetype.WAR, SecondChance::new),
    PERCHERON(BreedArchetype.DRAFT, SlowBlockImmunity::new),
    CLYDESDALE(BreedArchetype.DRAFT, Ironclad::new),
    SHIRE(BreedArchetype.DRAFT, Intimidation::new),
    BELGIAN(BreedArchetype.DRAFT, BrickBreak::new),
    ICELANDIC(BreedArchetype.PONY, HardyNorthern::new),
    MUSTANG(BreedArchetype.WAR, WildInstincts::new),
    HAFLINGER(BreedArchetype.PONY, Hearthlight::new),
    MORGAN(BreedArchetype.WAR, EasyKeeper::new),
    AMERICAN_PAINT(BreedArchetype.WESTERN),
    APPALOOSA(BreedArchetype.WESTERN, StockHorse::new),

    DONKEY_SPECIES(BreedArchetype.NONE),
    MULE_SPECIES(BreedArchetype.NONE),
    SKELETON_SPECIES(BreedArchetype.NONE),
    ZOMBIE_SPECIES(BreedArchetype.NONE),
    UNKNOWN_SPECIES(BreedArchetype.NONE);

    public record Coat(Variant color, Markings markings) {}

    private final BreedArchetype archetype;
    private final @Nullable Supplier<BreedAbility> ability;

    HorseBreed(BreedArchetype archetype) {
        this(archetype, null);
    }

    HorseBreed(BreedArchetype archetype, @Nullable Supplier<BreedAbility> ability) {
        this.archetype = archetype;
        this.ability = ability;
    }

    public BreedArchetype archetype() {
        return archetype;
    }

    public int chestRows(int bondTier) {
        return switch (this) {
            case BELGIAN -> 6;
            case HAFLINGER -> bondTier >= 2 ? 6 : 4;
            case MORGAN -> bondTier >= 2 ? 4 : archetype.chestRows();
            default -> archetype.chestRows();
        };
    }

    public @Nullable BreedAbility newAbility() {
        return ability == null ? null : ability.get();
    }

    private static final HorseBreed[] VALUES = values();
    public static final int HORSE_BREED_COUNT = 15;

    private static final Map<HorseBreed, List<Coat>> COAT_MAP = buildCoatMap();
    private static final Map<HorseBreed, List<ResourceKey<Biome>>> BIOME_MAP = buildBiomeMap();

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

    public List<Coat> allowedCoats() {
        return COAT_MAP.getOrDefault(this, List.of());
    }

    public Coat rollCoat(RandomSource random) {
        List<Coat> coats = allowedCoats();
        if (coats.isEmpty()) return null;
        return coats.get(random.nextInt(coats.size()));
    }

    public List<ResourceKey<Biome>> allowedBiomes() {
        return BIOME_MAP.getOrDefault(this, List.of());
    }

    public static Set<ResourceKey<Biome>> allBreedBiomes() {
        Set<ResourceKey<Biome>> out = new LinkedHashSet<>();
        for (HorseBreed breed : VALUES) {
            if (!breed.isRealBreed()) continue;
            out.addAll(breed.allowedBiomes());
        }
        return Collections.unmodifiableSet(out);
    }

    public static List<HorseBreed> breedsForBiome(ResourceKey<Biome> biome) {
        List<HorseBreed> matches = new ArrayList<>();
        for (HorseBreed breed : VALUES) {
            if (!breed.isRealBreed()) continue;
            if (breed.allowedBiomes().contains(biome)) {
                matches.add(breed);
            }
        }
        return matches;
    }

    public static List<HorseBreed> breedsMatchingCoat(Variant color, Markings markings) {
        List<HorseBreed> matches = new ArrayList<>();
        for (HorseBreed breed : VALUES) {
            if (!breed.isRealBreed()) continue;
            for (Coat coat : breed.allowedCoats()) {
                if (coat.color == color && coat.markings == markings) {
                    matches.add(breed);
                    break;
                }
            }
        }
        return matches;
    }

    public static HorseBreed speciesFor(AbstractHorse horse) {
        if (horse instanceof Horse) return null;
        if (horse instanceof Donkey) return DONKEY_SPECIES;
        if (horse instanceof Mule) return MULE_SPECIES;
        if (horse instanceof SkeletonHorse) return SKELETON_SPECIES;
        if (horse instanceof ZombieHorse) return ZOMBIE_SPECIES;
        return UNKNOWN_SPECIES;
    }

    private static Map<HorseBreed, List<Coat>> buildCoatMap() {
        EnumMap<HorseBreed, List<Coat>> map = new EnumMap<>(HorseBreed.class);

        map.put(THOROUGHBRED, List.of(
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.DARK_BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.WHITE),
                new Coat(Variant.CHESTNUT, Markings.WHITE)
        ));

        map.put(ARABIAN, List.of(
                new Coat(Variant.GRAY, Markings.NONE),
                new Coat(Variant.GRAY, Markings.WHITE),
                new Coat(Variant.WHITE, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE)
        ));

        map.put(QUARTER, List.of(
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.WHITE),
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.WHITE),
                new Coat(Variant.BLACK, Markings.WHITE),
                new Coat(Variant.CREAMY, Markings.WHITE)
        ));

        map.put(FRIESIAN, List.of(
                new Coat(Variant.BLACK, Markings.NONE)
        ));

        map.put(ANDALUSIAN, List.of(
                new Coat(Variant.GRAY, Markings.NONE),
                new Coat(Variant.WHITE, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE)
        ));

        map.put(PERCHERON, List.of(
                new Coat(Variant.GRAY, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.WHITE, Markings.NONE)
        ));

        map.put(CLYDESDALE, List.of(
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.BLACK, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD)
        ));

        map.put(SHIRE, List.of(
                new Coat(Variant.BLACK, Markings.WHITE_FIELD),
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.GRAY, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_FIELD)
        ));

        map.put(BELGIAN, List.of(
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.WHITE),
                new Coat(Variant.CREAMY, Markings.NONE),
                new Coat(Variant.CREAMY, Markings.WHITE)
        ));

        map.put(ICELANDIC, List.of(
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD),
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.BLACK, Markings.WHITE),
                new Coat(Variant.GRAY, Markings.WHITE_FIELD),
                new Coat(Variant.CREAMY, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE)
        ));

        map.put(MUSTANG, List.of(
                new Coat(Variant.DARK_BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.DARK_BROWN, Markings.WHITE),
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD)
        ));

        map.put(HAFLINGER, List.of(
                new Coat(Variant.CHESTNUT, Markings.WHITE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.CREAMY, Markings.WHITE)
        ));

        map.put(MORGAN, List.of(
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.DARK_BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.WHITE)
        ));

        map.put(AMERICAN_PAINT, List.of(
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.BLACK, Markings.WHITE_FIELD),
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.WHITE, Markings.WHITE_FIELD),
                new Coat(Variant.CREAMY, Markings.WHITE_FIELD)
        ));

        map.put(APPALOOSA, List.of(
                new Coat(Variant.WHITE, Markings.BLACK_DOTS),
                new Coat(Variant.GRAY, Markings.BLACK_DOTS),
                new Coat(Variant.CHESTNUT, Markings.WHITE_DOTS),
                new Coat(Variant.BROWN, Markings.WHITE_DOTS),
                new Coat(Variant.BLACK, Markings.WHITE_DOTS),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_DOTS)
        ));

        for (Map.Entry<HorseBreed, List<Coat>> entry : map.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<HorseBreed, List<ResourceKey<Biome>>> buildBiomeMap() {
        EnumMap<HorseBreed, List<ResourceKey<Biome>>> map = new EnumMap<>(HorseBreed.class);

        map.put(THOROUGHBRED, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW,
                Biomes.FOREST, Biomes.BIRCH_FOREST
        ));
        map.put(ARABIAN, List.of(
                Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS,
                Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA
        ));
        map.put(QUARTER, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS,
                Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA
        ));
        map.put(FRIESIAN, List.of(
                Biomes.DARK_FOREST, Biomes.FOREST, Biomes.PALE_GARDEN,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.TAIGA
        ));
        map.put(ANDALUSIAN, List.of(
                Biomes.MEADOW, Biomes.CHERRY_GROVE, Biomes.FLOWER_FOREST,
                Biomes.PLAINS, Biomes.FOREST
        ));
        map.put(PERCHERON, List.of(
                Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.FOREST,
                Biomes.MEADOW, Biomes.PLAINS
        ));
        map.put(CLYDESDALE, List.of(
                Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS,
                Biomes.WINDSWEPT_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA
        ));
        map.put(SHIRE, List.of(
                Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.WINDSWEPT_FOREST, Biomes.DARK_FOREST
        ));
        map.put(BELGIAN, List.of(
                Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.FOREST
        ));
        map.put(ICELANDIC, List.of(
                Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES,
                Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.SNOWY_SLOPES, Biomes.GROVE
        ));
        map.put(MUSTANG, List.of(
                Biomes.PLAINS, Biomes.SAVANNA, Biomes.WINDSWEPT_HILLS,
                Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.SPARSE_JUNGLE
        ));
        map.put(HAFLINGER, List.of(
                Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.MEADOW,
                Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.STONY_PEAKS
        ));
        map.put(MORGAN, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS,
                Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.MEADOW
        ));
        map.put(AMERICAN_PAINT, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS,
                Biomes.SAVANNA, Biomes.SPARSE_JUNGLE
        ));
        map.put(APPALOOSA, List.of(
                Biomes.PLAINS, Biomes.WOODED_BADLANDS,
                Biomes.SAVANNA_PLATEAU, Biomes.SUNFLOWER_PLAINS
        ));

        for (Map.Entry<HorseBreed, List<ResourceKey<Biome>>> entry : map.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }
}
