package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.entity.BhBreedCoats;
import icy.betterhorses.net.feature.breed.BrickBreak;
import icy.betterhorses.net.feature.breed.EasyKeeper;
import icy.betterhorses.net.feature.breed.Endurance;
import icy.betterhorses.net.feature.breed.FriesianPresence;
import icy.betterhorses.net.feature.breed.HardyNorthern;
import icy.betterhorses.net.feature.breed.Hearthlight;
import icy.betterhorses.net.feature.breed.Intimidation;
import icy.betterhorses.net.feature.breed.Ironclad;
import icy.betterhorses.net.feature.breed.SecondChance;
import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import icy.betterhorses.net.feature.breed.StandstillBurst;
import icy.betterhorses.net.feature.breed.StockHorse;
import icy.betterhorses.net.feature.breed.TopEnd;
import icy.betterhorses.net.feature.breed.WildInstincts;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BhContent {

    public static final DeferredRegister<ArchetypeType> ARCHETYPES =
            DeferredRegister.create(BhRegistries.ARCHETYPE_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<ArchetypeType, ArchetypeType> RACE = ARCHETYPES.register("race", () -> ArchetypeType.builder()
            .speed(0.2546D, 0.3472D).health(15.0D, 20.0D).jump(0.72D, 1.02D)
            .bashDamage(0.6D).bashKnockback(0.8D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .build());

    public static final DeferredHolder<ArchetypeType, ArchetypeType> WAR = ARCHETYPES.register("war", () -> ArchetypeType.builder()
            .speed(0.2083D, 0.3472D).health(25.0D, 40.0D).jump(0.57D, 0.97D)
            .bashDamage(1.5D).bashKnockback(1.2D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .suppressesRear(true).medkitMultiplier(2)
            .build());

    public static final DeferredHolder<ArchetypeType, ArchetypeType> WESTERN = ARCHETYPES.register("western", () -> ArchetypeType.builder()
            .speed(0.1852D, 0.3472D).health(15.0D, 30.0D).jump(0.57D, 0.84D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .pathSpeedBonus(0.10D, 0.20D, 0.50D)
            .build());

    public static final DeferredHolder<ArchetypeType, ArchetypeType> DRAFT = ARCHETYPES.register("draft", () -> ArchetypeType.builder()
            .speed(0.1620D, 0.2315D).health(35.0D, 50.0D).jump(0.38D, 0.72D)
            .bashDamage(1.25D).bashKnockback(2.0D)
            .defaultChestRows(4).allowsChestAndRiders(true)
            .baseSpookChance(0.05D).knockbackResistance(0.6D).allowsLargeCart(true)
            .build());

    public static final DeferredHolder<ArchetypeType, ArchetypeType> PONY = ARCHETYPES.register("pony", () -> ArchetypeType.builder()
            .speed(0.1852D, 0.3009D).health(25.0D, 40.0D).jump(0.57D, 0.84D)
            .bashDamage(0.75D).bashKnockback(0.8D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .passiveHealInterval(400).walksOnPowderSnow(true)
            .fallDamageWaiver(15.0D).stepHeight(2.0D)
            .build());

    public static final DeferredHolder<ArchetypeType, ArchetypeType> NONE = ARCHETYPES.register("none", () -> ArchetypeType.builder()
            .speed(0.1125D, 0.3375D).health(15.0D, 30.0D).jump(0.40D, 1.00D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .baseSpookChance(0.0D)
            .build());

    public static final DeferredRegister<AbilityType> ABILITIES =
            DeferredRegister.create(BhRegistries.ABILITY_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<AbilityType, AbilityType> TOP_END =
            ABILITIES.register("top_end", () -> new AbilityType(TopEnd::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> ENDURANCE =
            ABILITIES.register("endurance", () -> new AbilityType(Endurance::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> STANDSTILL_BURST =
            ABILITIES.register("standstill_burst", () -> new AbilityType(StandstillBurst::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> FRIESIAN_PRESENCE =
            ABILITIES.register("friesian_presence", () -> new AbilityType(FriesianPresence::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> SECOND_CHANCE =
            ABILITIES.register("second_chance", () -> new AbilityType(SecondChance::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> SLOW_BLOCK_IMMUNITY =
            ABILITIES.register("slow_block_immunity", () -> new AbilityType(SlowBlockImmunity::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> IRONCLAD =
            ABILITIES.register("ironclad", () -> new AbilityType(Ironclad::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> INTIMIDATION =
            ABILITIES.register("intimidation", () -> new AbilityType(Intimidation::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> BRICK_BREAK =
            ABILITIES.register("brick_break", () -> new AbilityType(BrickBreak::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> HARDY_NORTHERN =
            ABILITIES.register("hardy_northern", () -> new AbilityType(HardyNorthern::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> WILD_INSTINCTS =
            ABILITIES.register("wild_instincts", () -> new AbilityType(WildInstincts::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> HEARTHLIGHT =
            ABILITIES.register("hearthlight", () -> new AbilityType(Hearthlight::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> EASY_KEEPER =
            ABILITIES.register("easy_keeper", () -> new AbilityType(EasyKeeper::new, true));
    public static final DeferredHolder<AbilityType, AbilityType> STOCK_HORSE =
            ABILITIES.register("stock_horse", () -> new AbilityType(StockHorse::new, true));

    public static final DeferredRegister<BreedType> BREEDS =
            DeferredRegister.create(BhRegistries.BREED_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<BreedType, BreedType> THOROUGHBRED = BREEDS.register("thoroughbred", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.THOROUGHBRED.folder(), BhBreedCoats.THOROUGHBRED.coatIds(), BhBreedCoats.THOROUGHBRED.hasFoalVariant())
                    .entityType(entity("thoroughbred_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(TOP_END.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> ARABIAN = BREEDS.register("arabian", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ARABIAN.folder(), BhBreedCoats.ARABIAN.coatIds(), BhBreedCoats.ARABIAN.hasFoalVariant())
                    .entityType(entity("arabian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(ENDURANCE.getKey())
                    .stabilizerBody(StabilizerBody.SMALL)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> QUARTER = BREEDS.register("quarter", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.QUARTER.folder(), BhBreedCoats.QUARTER.coatIds(), BhBreedCoats.QUARTER.hasFoalVariant())
                    .entityType(entity("quarter_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(STANDSTILL_BURST.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> FRIESIAN = BREEDS.register("friesian", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.FRIESIAN.folder(), BhBreedCoats.FRIESIAN.coatIds(), BhBreedCoats.FRIESIAN.hasFoalVariant())
                    .entityType(entity("friesian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(FRIESIAN_PRESENCE.getKey())
                    .stabilizerBody(StabilizerBody.FRIESIAN)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> ANDALUSIAN = BREEDS.register("andalusian", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ANDALUSIAN.folder(), BhBreedCoats.ANDALUSIAN.coatIds(), BhBreedCoats.ANDALUSIAN.hasFoalVariant())
                    .entityType(entity("andalusian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(SECOND_CHANCE.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> PERCHERON = BREEDS.register("percheron", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.PERCHERON.folder(), BhBreedCoats.PERCHERON.coatIds(), BhBreedCoats.PERCHERON.hasFoalVariant())
                    .entityType(entity("percheron_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(SLOW_BLOCK_IMMUNITY.getKey())
                    .stabilizerBody(StabilizerBody.PERCHERON)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> CLYDESDALE = BREEDS.register("clydesdale", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.CLYDESDALE.folder(), BhBreedCoats.CLYDESDALE.coatIds(), BhBreedCoats.CLYDESDALE.hasFoalVariant())
                    .entityType(entity("clydesdale_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(IRONCLAD.getKey())
                    .stabilizerBody(StabilizerBody.PERCHERON)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> SHIRE = BREEDS.register("shire", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.SHIRE.folder(), BhBreedCoats.SHIRE.coatIds(), BhBreedCoats.SHIRE.hasFoalVariant())
                    .entityType(entity("shire_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(INTIMIDATION.getKey())
                    .stabilizerBody(StabilizerBody.SHIRE)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> BELGIAN = BREEDS.register("belgian", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.BELGIAN.folder(), BhBreedCoats.BELGIAN.coatIds(), BhBreedCoats.BELGIAN.hasFoalVariant())
                    .entityType(entity("belgian_horse"))
                    .chestRows(6).bondedChestRows(6)
                    .ability(BRICK_BREAK.getKey())
                    .stabilizerBody(StabilizerBody.BELGIAN)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> ICELANDIC = BREEDS.register("icelandic", () ->
            BreedType.builder(PONY.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ICELANDIC.folder(), BhBreedCoats.ICELANDIC.coatIds(), BhBreedCoats.ICELANDIC.hasFoalVariant())
                    .entityType(entity("icelandic_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(HARDY_NORTHERN.getKey())
                    .stabilizerBody(StabilizerBody.ICELANDIC)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> MUSTANG = BREEDS.register("mustang", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MUSTANG.folder(), BhBreedCoats.MUSTANG.coatIds(), BhBreedCoats.MUSTANG.hasFoalVariant())
                    .entityType(entity("mustang_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(WILD_INSTINCTS.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> HAFLINGER = BREEDS.register("haflinger", () ->
            BreedType.builder(PONY.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.HAFLINGER.folder(), BhBreedCoats.HAFLINGER.coatIds(), BhBreedCoats.HAFLINGER.hasFoalVariant())
                    .entityType(entity("haflinger_horse"))
                    .chestRows(4).bondedChestRows(6)
                    .ability(HEARTHLIGHT.getKey())
                    .stabilizerBody(StabilizerBody.HAFLINGER)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> MORGAN = BREEDS.register("morgan", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MORGAN.folder(), BhBreedCoats.MORGAN.coatIds(), BhBreedCoats.MORGAN.hasFoalVariant())
                    .entityType(entity("morgan_horse"))
                    .chestRows(3).bondedChestRows(4)
                    .ability(EASY_KEEPER.getKey())
                    .stabilizerBody(StabilizerBody.SMALL)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> AMERICAN_PAINT = BREEDS.register("american_paint", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.AMERICAN_PAINT.folder(), BhBreedCoats.AMERICAN_PAINT.coatIds(), BhBreedCoats.AMERICAN_PAINT.hasFoalVariant())
                    .entityType(entity("american_paint_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final DeferredHolder<BreedType, BreedType> APPALOOSA = BREEDS.register("appaloosa", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.APPALOOSA.folder(), BhBreedCoats.APPALOOSA.coatIds(), BhBreedCoats.APPALOOSA.hasFoalVariant())
                    .entityType(entity("appaloosa_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(STOCK_HORSE.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final DeferredRegister<GenderType> GENDERS =
            DeferredRegister.create(BhRegistries.GENDER_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<GenderType, GenderType> MALE = GENDERS.register("male", GenderType::new);
    public static final DeferredHolder<GenderType, GenderType> FEMALE = GENDERS.register("female", GenderType::new);

    public static final DeferredRegister<SpeciesType> SPECIES =
            DeferredRegister.create(BhRegistries.SPECIES_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<SpeciesType, SpeciesType> SPECIES_NONE = SPECIES.register("none", SpeciesType::new);
    public static final DeferredHolder<SpeciesType, SpeciesType> SPECIES_DONKEY = SPECIES.register("donkey", SpeciesType::new);
    public static final DeferredHolder<SpeciesType, SpeciesType> SPECIES_MULE = SPECIES.register("mule", SpeciesType::new);
    public static final DeferredHolder<SpeciesType, SpeciesType> SPECIES_SKELETON = SPECIES.register("skeleton", SpeciesType::new);
    public static final DeferredHolder<SpeciesType, SpeciesType> SPECIES_ZOMBIE = SPECIES.register("zombie", SpeciesType::new);

    public static final DeferredRegister<CommandType> COMMANDS =
            DeferredRegister.create(BhRegistries.COMMAND_TYPES, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<CommandType, CommandType> COMMAND_FOLLOW = COMMANDS.register("follow", CommandType::new);
    public static final DeferredHolder<CommandType, CommandType> COMMAND_STAY = COMMANDS.register("stay", CommandType::new);
    public static final DeferredHolder<CommandType, CommandType> COMMAND_RETURN_HOME = COMMANDS.register("return_home", CommandType::new);
    public static final DeferredHolder<CommandType, CommandType> COMMAND_SET_HOME = COMMANDS.register("set_home", CommandType::new);
    public static final DeferredHolder<CommandType, CommandType> COMMAND_WANDER = COMMANDS.register("wander", CommandType::new);
    public static final DeferredHolder<CommandType, CommandType> COMMAND_ABILITY = COMMANDS.register("ability", CommandType::new);

    private BhContent() {
    }

    private static ResourceKey<EntityType<?>> entity(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path));
    }

    public static void register(IEventBus modEventBus) {
        ARCHETYPES.register(modEventBus);
        ABILITIES.register(modEventBus);
        BREEDS.register(modEventBus);
        GENDERS.register(modEventBus);
        SPECIES.register(modEventBus);
        COMMANDS.register(modEventBus);
    }

    public static void logSummary() {
        IcysBetterHorses.LOGGER.info("[content] {} archetypes, {} abilities, {} breeds registered",
                ARCHETYPES.getEntries().size(), ABILITIES.getEntries().size(), BREEDS.getEntries().size());
    }
}
