package icy.betterhorses.net.registry;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class BhBreeds {

    private BhBreeds() {
    }

    public static @Nullable ResourceKey<BreedType> keyOf(HorseBreed breed) {
        if (!breed.isRealBreed()) {
            return null;
        }
        return ResourceKey.create(BhRegistries.BREED_TYPES, Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, breed.id()));
    }

    public static ResourceKey<SpeciesType> speciesOf(HorseBreed breed) {
        return switch (breed) {
            case DONKEY_SPECIES -> BhContent.SPECIES_DONKEY.key();
            case MULE_SPECIES -> BhContent.SPECIES_MULE.key();
            case SKELETON_SPECIES -> BhContent.SPECIES_SKELETON.key();
            case ZOMBIE_SPECIES -> BhContent.SPECIES_ZOMBIE.key();
            default -> BhContent.SPECIES_NONE.key();
        };
    }
}
