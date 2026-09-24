package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class BhRegistries {

    public static final ResourceKey<Registry<ArchetypeType>> ARCHETYPE_TYPES =
            ResourceKey.createRegistryKey(id("archetype_types"));
    public static final ResourceKey<Registry<AbilityType>> ABILITY_TYPES =
            ResourceKey.createRegistryKey(id("ability_types"));
    public static final ResourceKey<Registry<BreedType>> BREED_TYPES =
            ResourceKey.createRegistryKey(id("breed_types"));
    public static final ResourceKey<Registry<GenderType>> GENDER_TYPES =
            ResourceKey.createRegistryKey(id("gender_types"));
    public static final ResourceKey<Registry<SpeciesType>> SPECIES_TYPES =
            ResourceKey.createRegistryKey(id("species_types"));
    public static final ResourceKey<Registry<CommandType>> COMMAND_TYPES =
            ResourceKey.createRegistryKey(id("command_types"));

    private static final Registry<ArchetypeType> ARCHETYPES = build(ARCHETYPE_TYPES);
    private static final Registry<AbilityType> ABILITIES = build(ABILITY_TYPES);
    private static final Registry<BreedType> BREEDS = build(BREED_TYPES);
    private static final Registry<GenderType> GENDERS = build(GENDER_TYPES);
    private static final Registry<SpeciesType> SPECIES = build(SPECIES_TYPES);
    private static final Registry<CommandType> COMMANDS = build(COMMAND_TYPES);

    private BhRegistries() {
    }

    private static <T> MappedRegistry<T> build(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.create(key).attribute(RegistryAttribute.SYNCED).buildAndRegister();
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

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
    }
}
