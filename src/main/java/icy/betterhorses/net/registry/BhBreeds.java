package icy.betterhorses.net.registry;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseSpecies;
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

    public static HorseSpecies speciesOf(HorseBreed breed) {
        return switch (breed) {
            case DONKEY_SPECIES -> HorseSpecies.DONKEY;
            case MULE_SPECIES -> HorseSpecies.MULE;
            case SKELETON_SPECIES -> HorseSpecies.SKELETON;
            case ZOMBIE_SPECIES -> HorseSpecies.ZOMBIE;
            default -> HorseSpecies.NONE;
        };
    }
}
