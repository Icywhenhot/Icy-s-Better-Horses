package icy.betterhorses.net.registry;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class BhBreeds {

    private BhBreeds() {
    }

    public static @Nullable ResourceKey<BreedType> keyOf(HorseBreed breed) {
        if (!breed.isRealBreed()) {
            return null;
        }
        return ResourceKey.create(BhRegistries.BREED_TYPES, new ResourceLocation(IcysBetterHorses.MOD_ID, breed.id()));
    }

    public static ResourceKey<SpeciesType> speciesOf(HorseBreed breed) {
        return switch (breed) {
            case DONKEY_SPECIES -> BhContent.SPECIES_DONKEY.getKey();
            case MULE_SPECIES -> BhContent.SPECIES_MULE.getKey();
            case SKELETON_SPECIES -> BhContent.SPECIES_SKELETON.getKey();
            case ZOMBIE_SPECIES -> BhContent.SPECIES_ZOMBIE.getKey();
            default -> BhContent.SPECIES_NONE.getKey();
        };
    }
}
