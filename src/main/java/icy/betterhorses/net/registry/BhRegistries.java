package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

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

    private static final Registry<ArchetypeType> ARCHETYPES = new RegistryBuilder<>(ARCHETYPE_TYPES).create();
    private static final Registry<AbilityType> ABILITIES = new RegistryBuilder<>(ABILITY_TYPES).create();
    private static final Registry<BreedType> BREEDS = new RegistryBuilder<>(BREED_TYPES).create();
    private static final Registry<GenderType> GENDERS = new RegistryBuilder<>(GENDER_TYPES).create();
    private static final Registry<SpeciesType> SPECIES = new RegistryBuilder<>(SPECIES_TYPES).create();
    private static final Registry<CommandType> COMMANDS = new RegistryBuilder<>(COMMAND_TYPES).create();

    private BhRegistries() {
    }

    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(ARCHETYPES);
        event.register(ABILITIES);
        event.register(BREEDS);
        event.register(GENDERS);
        event.register(SPECIES);
        event.register(COMMANDS);
    }

    public static Registry<ArchetypeType> archetypeTypeRegistry() {
        return ARCHETYPES;
    }

    public static Registry<AbilityType> abilityTypeRegistry() {
        return ABILITIES;
    }

    public static Registry<BreedType> breedTypeRegistry() {
        return BREEDS;
    }

    public static Registry<GenderType> genderTypeRegistry() {
        return GENDERS;
    }

    public static Registry<SpeciesType> speciesTypeRegistry() {
        return SPECIES;
    }

    public static Registry<CommandType> commandTypeRegistry() {
        return COMMANDS;
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path);
    }
}
