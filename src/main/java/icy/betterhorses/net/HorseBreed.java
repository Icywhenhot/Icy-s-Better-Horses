package icy.betterhorses.net;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.animal.horse.ZombieHorse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Horse breed identifier. The first 15 entries are real horse breeds eligible
 * for the breeding system. The trailing entries are species placeholders used
 * to label non-horse AbstractHorse subclasses on the info screen.
 *
 * <p>Each real breed declares a list of allowed vanilla coat combinations
 * ({@link Variant} colour × {@link Markings}). A horse's breed determines which
 * coats it can roll; conversely, a pre-existing horse's coat is used to infer
 * which breed it should be classified as.
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

    public record Coat(Variant color, Markings markings) {}

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

    /** Coat combinations allowed for this breed, or empty for species placeholders. */
    public List<Coat> allowedCoats() {
        return COAT_MAP.getOrDefault(this, List.of());
    }

    /** Pick a random allowed coat for this breed, or null if the breed has no coat list. */
    public Coat rollCoat(RandomSource random) {
        List<Coat> coats = allowedCoats();
        if (coats.isEmpty()) return null;
        return coats.get(random.nextInt(coats.size()));
    }

    /** Biomes in which this breed naturally spawns, or empty for species placeholders. */
    public List<ResourceKey<Biome>> allowedBiomes() {
        return BIOME_MAP.getOrDefault(this, List.of());
    }

    /** Every biome that appears in at least one breed's spawn list (used for BiomeModifications.addSpawn). */
    public static java.util.Set<ResourceKey<Biome>> allBreedBiomes() {
        java.util.Set<ResourceKey<Biome>> out = new java.util.LinkedHashSet<>();
        for (HorseBreed breed : VALUES) {
            if (!breed.isRealBreed()) continue;
            out.addAll(breed.allowedBiomes());
        }
        return java.util.Collections.unmodifiableSet(out);
    }

    /** All real-horse breeds that include {@code biome} in their allowed-biome list. */
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

    /**
     * Find real-horse breeds whose allowed-coat list contains the given coat.
     * Used to classify pre-existing horses by their current appearance.
     */
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

    /** Returns the species placeholder for non-horse AbstractHorse subclasses, or null if it's a real horse. */
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

        // Thoroughbred — racing horse: bay, chestnut, black, dark bay. Subtle markings.
        map.put(THOROUGHBRED, List.of(
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.DARK_BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.WHITE),
                new Coat(Variant.CHESTNUT, Markings.WHITE)
        ));

        // Arabian — refined desert horse, predominantly gray, also bay/chestnut/black.
        map.put(ARABIAN, List.of(
                new Coat(Variant.GRAY, Markings.NONE),
                new Coat(Variant.GRAY, Markings.WHITE),
                new Coat(Variant.WHITE, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE)
        ));

        // Quarter Horse — American versatility. Sorrel/chestnut, bay, black, palomino. Often with face/leg white.
        map.put(QUARTER, List.of(
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.WHITE),
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.WHITE),
                new Coat(Variant.BLACK, Markings.WHITE),
                new Coat(Variant.CREAMY, Markings.WHITE)
        ));

        // Friesian — almost exclusively solid black, no white.
        map.put(FRIESIAN, List.of(
                new Coat(Variant.BLACK, Markings.NONE)
        ));

        // Andalusian — Spanish baroque, mostly gray, then white, bay, black.
        map.put(ANDALUSIAN, List.of(
                new Coat(Variant.GRAY, Markings.NONE),
                new Coat(Variant.WHITE, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE)
        ));

        // Percheron — French draft, gray or black, clean coat.
        map.put(PERCHERON, List.of(
                new Coat(Variant.GRAY, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.WHITE, Markings.NONE)
        ));

        // Clydesdale — Scottish draft, famous for big white feathered socks (WHITE_FIELD).
        map.put(CLYDESDALE, List.of(
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.BLACK, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD)
        ));

        // Shire — English draft, similar to Clydesdale, often black or bay with white socks.
        map.put(SHIRE, List.of(
                new Coat(Variant.BLACK, Markings.WHITE_FIELD),
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.GRAY, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_FIELD)
        ));

        // Belgian — chestnut/sorrel with flaxen (cream-like) mane.
        map.put(BELGIAN, List.of(
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.WHITE),
                new Coat(Variant.CREAMY, Markings.NONE),
                new Coat(Variant.CREAMY, Markings.WHITE)
        ));

        // Icelandic — small, hardy, many colors, often with white markings.
        map.put(ICELANDIC, List.of(
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD),
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.BLACK, Markings.WHITE),
                new Coat(Variant.GRAY, Markings.WHITE_FIELD),
                new Coat(Variant.CREAMY, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE)
        ));

        // Mustang — wild American, broad palette, mostly solid earthy tones.
        map.put(MUSTANG, List.of(
                new Coat(Variant.DARK_BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.DARK_BROWN, Markings.WHITE),
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD)
        ));

        // Haflinger — always chestnut with flaxen mane, often with a white blaze.
        map.put(HAFLINGER, List.of(
                new Coat(Variant.CHESTNUT, Markings.WHITE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.CREAMY, Markings.WHITE)
        ));

        // Morgan — classic American breed: bay, chestnut, black, dark brown.
        map.put(MORGAN, List.of(
                new Coat(Variant.BROWN, Markings.NONE),
                new Coat(Variant.CHESTNUT, Markings.NONE),
                new Coat(Variant.BLACK, Markings.NONE),
                new Coat(Variant.DARK_BROWN, Markings.NONE),
                new Coat(Variant.BROWN, Markings.WHITE)
        ));

        // American Paint — pinto. Any base color with extensive white field patches.
        map.put(AMERICAN_PAINT, List.of(
                new Coat(Variant.BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.BLACK, Markings.WHITE_FIELD),
                new Coat(Variant.CHESTNUT, Markings.WHITE_FIELD),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_FIELD),
                new Coat(Variant.WHITE, Markings.WHITE_FIELD),
                new Coat(Variant.CREAMY, Markings.WHITE_FIELD)
        ));

        // Appaloosa — spotted, leopard / blanket patterns (the only dotted patterns).
        map.put(APPALOOSA, List.of(
                new Coat(Variant.WHITE, Markings.BLACK_DOTS),
                new Coat(Variant.GRAY, Markings.BLACK_DOTS),
                new Coat(Variant.CHESTNUT, Markings.WHITE_DOTS),
                new Coat(Variant.BROWN, Markings.WHITE_DOTS),
                new Coat(Variant.BLACK, Markings.WHITE_DOTS),
                new Coat(Variant.DARK_BROWN, Markings.WHITE_DOTS)
        ));

        // Make each list immutable.
        for (Map.Entry<HorseBreed, List<Coat>> entry : map.entrySet()) {
            entry.setValue(Collections.unmodifiableList(entry.getValue()));
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<HorseBreed, List<ResourceKey<Biome>>> buildBiomeMap() {
        EnumMap<HorseBreed, List<ResourceKey<Biome>>> map = new EnumMap<>(HorseBreed.class);

        // Thoroughbred — temperate British racing horse: open grass + light woodland.
        map.put(THOROUGHBRED, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW,
                Biomes.FOREST, Biomes.BIRCH_FOREST
        ));
        // Arabian — desert origin: arid biomes.
        map.put(ARABIAN, List.of(
                Biomes.DESERT, Biomes.BADLANDS, Biomes.ERODED_BADLANDS,
                Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA
        ));
        // Quarter Horse — North American grasslands.
        map.put(QUARTER, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS,
                Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA
        ));
        // Friesian — Dutch lowland and forest origin.
        map.put(FRIESIAN, List.of(
                Biomes.DARK_FOREST, Biomes.FOREST,
                Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.TAIGA
        ));
        // Andalusian — Iberian peninsula: open meadows + light woodland.
        map.put(ANDALUSIAN, List.of(
                Biomes.MEADOW, Biomes.CHERRY_GROVE, Biomes.FLOWER_FOREST,
                Biomes.PLAINS, Biomes.FOREST
        ));
        // Percheron — French farmland: open temperate biomes.
        map.put(PERCHERON, List.of(
                Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.FOREST,
                Biomes.MEADOW, Biomes.PLAINS
        ));
        // Clydesdale — Scottish highlands: rough, gravelly, mountainous.
        map.put(CLYDESDALE, List.of(
                Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS,
                Biomes.WINDSWEPT_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA
        ));
        // Shire — English forests and farmland.
        map.put(SHIRE, List.of(
                Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.WINDSWEPT_FOREST, Biomes.DARK_FOREST
        ));
        // Belgian — cold-tolerant European draft.
        map.put(BELGIAN, List.of(
                Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.FOREST
        ));
        // Icelandic — built for arctic conditions.
        map.put(ICELANDIC, List.of(
                Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES,
                Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.SNOWY_SLOPES, Biomes.GROVE
        ));
        // Mustang — wild, versatile, found nearly anywhere the old American west could be.
        map.put(MUSTANG, List.of(
                Biomes.PLAINS, Biomes.SAVANNA, Biomes.WINDSWEPT_HILLS,
                Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.SPARSE_JUNGLE
        ));
        // Haflinger — Alpine breed, comfortable on rocky high ground.
        map.put(HAFLINGER, List.of(
                Biomes.SNOWY_SLOPES, Biomes.GROVE, Biomes.MEADOW,
                Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.STONY_PEAKS
        ));
        // Morgan — American versatility: temperate plains and forests.
        map.put(MORGAN, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS,
                Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.MEADOW
        ));
        // American Paint — colour breed of the American grasslands.
        map.put(AMERICAN_PAINT, List.of(
                Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS,
                Biomes.SAVANNA, Biomes.SPARSE_JUNGLE
        ));
        // Appaloosa — Nez Perce horse: high plateau, plains, rough badlands.
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
