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
    public static final ResourceKey<Registry<GenderType>> GENDER_TYPES =
            ResourceKey.createRegistryKey(rl("gender_types"));
    public static final ResourceKey<Registry<SpeciesType>> SPECIES_TYPES =
            ResourceKey.createRegistryKey(rl("species_types"));
    public static final ResourceKey<Registry<CommandType>> COMMAND_TYPES =
            ResourceKey.createRegistryKey(rl("command_types"));

    private static Supplier<IForgeRegistry<ArchetypeType>> archetypeTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<AbilityType>> abilityTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<BreedType>> breedTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<GenderType>> genderTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<SpeciesType>> speciesTypeRegistrySupplier;
    private static Supplier<IForgeRegistry<CommandType>> commandTypeRegistrySupplier;

    private BhRegistries() {
    }

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        archetypeTypeRegistrySupplier = event.create(new RegistryBuilder<ArchetypeType>().setName(ARCHETYPE_TYPES.location()));
        abilityTypeRegistrySupplier = event.create(new RegistryBuilder<AbilityType>().setName(ABILITY_TYPES.location()));
        breedTypeRegistrySupplier = event.create(new RegistryBuilder<BreedType>().setName(BREED_TYPES.location()));
        genderTypeRegistrySupplier = event.create(new RegistryBuilder<GenderType>().setName(GENDER_TYPES.location()));
        speciesTypeRegistrySupplier = event.create(new RegistryBuilder<SpeciesType>().setName(SPECIES_TYPES.location()));
        commandTypeRegistrySupplier = event.create(new RegistryBuilder<CommandType>().setName(COMMAND_TYPES.location()));
        IcysBetterHorses.LOGGER.info(
                "[registry] created archetype_types, ability_types, breed_types, gender_types, species_types, command_types");
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

    public static IForgeRegistry<GenderType> genderTypeRegistry() {
        return genderTypeRegistrySupplier.get();
    }

    public static IForgeRegistry<SpeciesType> speciesTypeRegistry() {
        return speciesTypeRegistrySupplier.get();
    }

    public static IForgeRegistry<CommandType> commandTypeRegistry() {
        return commandTypeRegistrySupplier.get();
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path);
    }
}
