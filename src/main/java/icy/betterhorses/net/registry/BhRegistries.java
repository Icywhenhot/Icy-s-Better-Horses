package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class BhRegistries {

    public static final ResourceKey<Registry<ArchetypeType>> ARCHETYPE_TYPES =
            ResourceKey.createRegistryKey(rl("archetype_types"));
    public static final ResourceKey<Registry<AbilityType>> ABILITY_TYPES =
            ResourceKey.createRegistryKey(rl("ability_types"));
    public static final ResourceKey<Registry<BreedType>> BREED_TYPES =
            ResourceKey.createRegistryKey(rl("breed_types"));

    private static Supplier<IForgeRegistry<ArchetypeType>> archetypeTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<AbilityType>> abilityTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<BreedType>> breedTypeRegistrySupplier;

    private BhRegistries() {
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        archetypeTypeRegistrySupplier = event.create(new RegistryBuilder<ArchetypeType>().setName(ARCHETYPE_TYPES.location()));
        abilityTypeRegistrySupplier = event.create(new RegistryBuilder<AbilityType>().setName(ABILITY_TYPES.location()));
        breedTypeRegistrySupplier = event.create(new RegistryBuilder<BreedType>().setName(BREED_TYPES.location()));
        IcysBetterHorses.LOGGER.info("[registry] created archetype_types, ability_types, breed_types");
    }

    public static IForgeRegistry<ArchetypeType> archetypeTypeRegistry() {
        return archetypeTypeRegistrySupplier.get();
    }

    public static IForgeRegistry<AbilityType> abilityTypeRegistry() {
        return abilityTypeRegistrySupplier.get();
    }

    public static IForgeRegistry<BreedType> breedTypeRegistry() {
        return breedTypeRegistrySupplier.get();
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path);
    }
}
