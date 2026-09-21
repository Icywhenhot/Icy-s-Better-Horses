package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModEntities;
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
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class
BhContent {

    public static final DeferredRegister<ArchetypeType> ARCHETYPES =
            DeferredRegister.create(BhRegistries.ARCHETYPE_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<ArchetypeType> RACE = ARCHETYPES.register("race", () -> ArchetypeType.builder()
            .speed(0.2546D, 0.3472D).health(15.0D, 20.0D).jump(0.72D, 1.02D)
            .bashDamage(0.6D).bashKnockback(0.8D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .build());

    public static final RegistryObject<ArchetypeType> WAR = ARCHETYPES.register("war", () -> ArchetypeType.builder()
            .speed(0.2083D, 0.3472D).health(25.0D, 40.0D).jump(0.57D, 0.97D)
            .bashDamage(1.5D).bashKnockback(1.2D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .suppressesRear(true).medkitMultiplier(2)
            .build());

    public static final RegistryObject<ArchetypeType> WESTERN = ARCHETYPES.register("western", () -> ArchetypeType.builder()
            .speed(0.1852D, 0.3472D).health(15.0D, 30.0D).jump(0.57D, 0.84D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .pathSpeedBonus(0.10D, 0.20D, 0.50D)
            .build());

    public static final RegistryObject<ArchetypeType> DRAFT = ARCHETYPES.register("draft", () -> ArchetypeType.builder()
            .speed(0.1620D, 0.2315D).health(35.0D, 50.0D).jump(0.38D, 0.72D)
            .bashDamage(1.25D).bashKnockback(2.0D)
            .defaultChestRows(4).allowsChestAndRiders(true)
            .baseSpookChance(0.05D).knockbackResistance(0.6D).allowsLargeCart(true)
            .build());

    public static final RegistryObject<ArchetypeType> PONY = ARCHETYPES.register("pony", () -> ArchetypeType.builder()
            .speed(0.1852D, 0.3009D).health(25.0D, 40.0D).jump(0.57D, 0.84D)
            .bashDamage(0.75D).bashKnockback(0.8D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .passiveHealInterval(400).walksOnPowderSnow(true)
            .fallDamageWaiver(15.0D).stepHeight(2.0D)
            .build());

    public static final RegistryObject<ArchetypeType> NONE = ARCHETYPES.register("none", () -> ArchetypeType.builder()
            .speed(0.1125D, 0.3375D).health(15.0D, 30.0D).jump(0.40D, 1.00D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .baseSpookChance(0.0D)
            .build());

    public static final DeferredRegister<AbilityType> ABILITIES =
            DeferredRegister.create(BhRegistries.ABILITY_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<AbilityType> TOP_END =
            ABILITIES.register("top_end", () -> new AbilityType(TopEnd::new, true));
    public static final RegistryObject<AbilityType> ENDURANCE =
            ABILITIES.register("endurance", () -> new AbilityType(Endurance::new, true));
    public static final RegistryObject<AbilityType> STANDSTILL_BURST =
            ABILITIES.register("standstill_burst", () -> new AbilityType(StandstillBurst::new, true));
    public static final RegistryObject<AbilityType> FRIESIAN_PRESENCE =
            ABILITIES.register("friesian_presence", () -> new AbilityType(FriesianPresence::new, true));
    public static final RegistryObject<AbilityType> SECOND_CHANCE =
            ABILITIES.register("second_chance", () -> new AbilityType(SecondChance::new, true));
    public static final RegistryObject<AbilityType> SLOW_BLOCK_IMMUNITY =
            ABILITIES.register("slow_block_immunity", () -> new AbilityType(SlowBlockImmunity::new, true));
    public static final RegistryObject<AbilityType> IRONCLAD =
            ABILITIES.register("ironclad", () -> new AbilityType(Ironclad::new, true));
    public static final RegistryObject<AbilityType> INTIMIDATION =
            ABILITIES.register("intimidation", () -> new AbilityType(Intimidation::new, true));
    public static final RegistryObject<AbilityType> BRICK_BREAK =
            ABILITIES.register("brick_break", () -> new AbilityType(BrickBreak::new, true));
    public static final RegistryObject<AbilityType> HARDY_NORTHERN =
            ABILITIES.register("hardy_northern", () -> new AbilityType(HardyNorthern::new, true));
    public static final RegistryObject<AbilityType> WILD_INSTINCTS =
            ABILITIES.register("wild_instincts", () -> new AbilityType(WildInstincts::new, true));
    public static final RegistryObject<AbilityType> HEARTHLIGHT =
            ABILITIES.register("hearthlight", () -> new AbilityType(Hearthlight::new, true));
    public static final RegistryObject<AbilityType> EASY_KEEPER =
            ABILITIES.register("easy_keeper", () -> new AbilityType(EasyKeeper::new, true));
    public static final RegistryObject<AbilityType> STOCK_HORSE =
            ABILITIES.register("stock_horse", () -> new AbilityType(StockHorse::new, true));

    public static final DeferredRegister<BreedType> BREEDS =
            DeferredRegister.create(BhRegistries.BREED_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<BreedType> THOROUGHBRED = BREEDS.register("thoroughbred", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.THOROUGHBRED.folder(), BhBreedCoats.THOROUGHBRED.coatIds(), BhBreedCoats.THOROUGHBRED.hasFoalVariant())
                    .entityType(ModEntities.THOROUGHBRED_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(TOP_END.getKey())
                    .build());
    
    public static final RegistryObject<BreedType> ARABIAN = BREEDS.register("arabian", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ARABIAN.folder(), BhBreedCoats.ARABIAN.coatIds(), BhBreedCoats.ARABIAN.hasFoalVariant())
                    .entityType(ModEntities.ARABIAN_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(ENDURANCE.getKey())
                    .build());

    public static final RegistryObject<BreedType> QUARTER = BREEDS.register("quarter", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.QUARTER.folder(), BhBreedCoats.QUARTER.coatIds(), BhBreedCoats.QUARTER.hasFoalVariant())
                    .entityType(ModEntities.QUARTER_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(STANDSTILL_BURST.getKey())
                    .build());

    public static final RegistryObject<BreedType> FRIESIAN = BREEDS.register("friesian", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.FRIESIAN.folder(), BhBreedCoats.FRIESIAN.coatIds(), BhBreedCoats.FRIESIAN.hasFoalVariant())
                    .entityType(ModEntities.FRIESIAN_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(FRIESIAN_PRESENCE.getKey())
                    .build());

    public static final RegistryObject<BreedType> ANDALUSIAN = BREEDS.register("andalusian", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ANDALUSIAN.folder(), BhBreedCoats.ANDALUSIAN.coatIds(), BhBreedCoats.ANDALUSIAN.hasFoalVariant())
                    .entityType(ModEntities.ANDALUSIAN_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(SECOND_CHANCE.getKey())
                    .build());

    public static final RegistryObject<BreedType> PERCHERON = BREEDS.register("percheron", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.PERCHERON.folder(), BhBreedCoats.PERCHERON.coatIds(), BhBreedCoats.PERCHERON.hasFoalVariant())
                    .entityType(ModEntities.PERCHERON_HORSE.getKey())
                    .chestRows(4).bondedChestRows(4)
                    .ability(SLOW_BLOCK_IMMUNITY.getKey())
                    .build());

    public static final RegistryObject<BreedType> CLYDESDALE = BREEDS.register("clydesdale", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.CLYDESDALE.folder(), BhBreedCoats.CLYDESDALE.coatIds(), BhBreedCoats.CLYDESDALE.hasFoalVariant())
                    .entityType(ModEntities.CLYDESDALE_HORSE.getKey())
                    .chestRows(4).bondedChestRows(4)
                    .ability(IRONCLAD.getKey())
                    .build());

    public static final RegistryObject<BreedType> SHIRE = BREEDS.register("shire", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.SHIRE.folder(), BhBreedCoats.SHIRE.coatIds(), BhBreedCoats.SHIRE.hasFoalVariant())
                    .entityType(ModEntities.SHIRE_HORSE.getKey())
                    .chestRows(4).bondedChestRows(4)
                    .ability(INTIMIDATION.getKey())
                    .build());

    public static final RegistryObject<BreedType> BELGIAN = BREEDS.register("belgian", () ->
            BreedType.builder(DRAFT.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.BELGIAN.folder(), BhBreedCoats.BELGIAN.coatIds(), BhBreedCoats.BELGIAN.hasFoalVariant())
                    .entityType(ModEntities.BELGIAN_HORSE.getKey())
                    .chestRows(6).bondedChestRows(6)
                    .ability(BRICK_BREAK.getKey())
                    .build());

    public static final RegistryObject<BreedType> ICELANDIC = BREEDS.register("icelandic", () ->
            BreedType.builder(PONY.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ICELANDIC.folder(), BhBreedCoats.ICELANDIC.coatIds(), BhBreedCoats.ICELANDIC.hasFoalVariant())
                    .entityType(ModEntities.ICELANDIC_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(HARDY_NORTHERN.getKey())
                    .build());

    public static final RegistryObject<BreedType> MUSTANG = BREEDS.register("mustang", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MUSTANG.folder(), BhBreedCoats.MUSTANG.coatIds(), BhBreedCoats.MUSTANG.hasFoalVariant())
                    .entityType(ModEntities.MUSTANG_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(WILD_INSTINCTS.getKey())
                    .build());

    public static final RegistryObject<BreedType> HAFLINGER = BREEDS.register("haflinger", () ->
            BreedType.builder(PONY.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.HAFLINGER.folder(), BhBreedCoats.HAFLINGER.coatIds(), BhBreedCoats.HAFLINGER.hasFoalVariant())
                    .entityType(ModEntities.HAFLINGER_HORSE.getKey())
                    .chestRows(4).bondedChestRows(6)
                    .ability(HEARTHLIGHT.getKey())
                    .build());

    public static final RegistryObject<BreedType> MORGAN = BREEDS.register("morgan", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MORGAN.folder(), BhBreedCoats.MORGAN.coatIds(), BhBreedCoats.MORGAN.hasFoalVariant())
                    .entityType(ModEntities.MORGAN_HORSE.getKey())
                    .chestRows(3).bondedChestRows(4)
                    .ability(EASY_KEEPER.getKey())
                    .build());

    public static final RegistryObject<BreedType> AMERICAN_PAINT = BREEDS.register("american_paint", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.AMERICAN_PAINT.folder(), BhBreedCoats.AMERICAN_PAINT.coatIds(), BhBreedCoats.AMERICAN_PAINT.hasFoalVariant())
                    .entityType(ModEntities.AMERICAN_PAINT_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .build());

    public static final RegistryObject<BreedType> APPALOOSA = BREEDS.register("appaloosa", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.APPALOOSA.folder(), BhBreedCoats.APPALOOSA.coatIds(), BhBreedCoats.APPALOOSA.hasFoalVariant())
                    .entityType(ModEntities.APPALOOSA_HORSE.getKey())
                    .chestRows(3).bondedChestRows(3)
                    .ability(STOCK_HORSE.getKey())
                    .build());

    public static final DeferredRegister<GenderType> GENDERS =
            DeferredRegister.create(BhRegistries.GENDER_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<GenderType> MALE = GENDERS.register("male", GenderType::new);
    public static final RegistryObject<GenderType> FEMALE = GENDERS.register("female", GenderType::new);

    public static final DeferredRegister<SpeciesType> SPECIES =
            DeferredRegister.create(BhRegistries.SPECIES_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<SpeciesType> SPECIES_NONE = SPECIES.register("none", SpeciesType::new);
    public static final RegistryObject<SpeciesType> SPECIES_DONKEY = SPECIES.register("donkey", SpeciesType::new);
    public static final RegistryObject<SpeciesType> SPECIES_MULE = SPECIES.register("mule", SpeciesType::new);
    public static final RegistryObject<SpeciesType> SPECIES_SKELETON = SPECIES.register("skeleton", SpeciesType::new);
    public static final RegistryObject<SpeciesType> SPECIES_ZOMBIE = SPECIES.register("zombie", SpeciesType::new);

    public static final DeferredRegister<CommandType> COMMANDS =
            DeferredRegister.create(BhRegistries.COMMAND_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<CommandType> COMMAND_FOLLOW = COMMANDS.register("follow", CommandType::new);
    public static final RegistryObject<CommandType> COMMAND_STAY = COMMANDS.register("stay", CommandType::new);
    public static final RegistryObject<CommandType> COMMAND_RETURN_HOME = COMMANDS.register("return_home", CommandType::new);
    public static final RegistryObject<CommandType> COMMAND_SET_HOME = COMMANDS.register("set_home", CommandType::new);
    public static final RegistryObject<CommandType> COMMAND_WANDER = COMMANDS.register("wander", CommandType::new);
    public static final RegistryObject<CommandType> COMMAND_ABILITY = COMMANDS.register("ability", CommandType::new);

    private BhContent() {
    }

    public static void register(net.minecraftforge.eventbus.api.IEventBus modEventBus) {
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
