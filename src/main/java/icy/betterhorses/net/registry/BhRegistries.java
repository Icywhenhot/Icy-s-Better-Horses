package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

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

    private static final Registry<ArchetypeType> ARCHETYPES = FabricRegistryBuilder.createSimple(ARCHETYPE_TYPES).buildAndRegister();
    private static final Registry<AbilityType> ABILITIES = FabricRegistryBuilder.createSimple(ABILITY_TYPES).buildAndRegister();
    private static final Registry<BreedType> BREEDS = FabricRegistryBuilder.createSimple(BREED_TYPES).buildAndRegister();
    private static final Registry<GenderType> GENDERS = FabricRegistryBuilder.createSimple(GENDER_TYPES).buildAndRegister();
    private static final Registry<SpeciesType> SPECIES = FabricRegistryBuilder.createSimple(SPECIES_TYPES).buildAndRegister();
    private static final Registry<CommandType> COMMANDS = FabricRegistryBuilder.createSimple(COMMAND_TYPES).buildAndRegister();

    private BhRegistries() {
    }

    public static void register() {
        IcysBetterHorses.LOGGER.info(
                "[registry] created archetype_types, ability_types, breed_types, gender_types, species_types, command_types");
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
        return new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path);
    }
}
