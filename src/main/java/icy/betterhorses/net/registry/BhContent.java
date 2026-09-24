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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public final class BhContent {

    public static final Holder.Reference<ArchetypeType> RACE = register(BhRegistries.archetypeTypeRegistry(), "race", ArchetypeType.builder()
            .speed(0.2546D, 0.3472D).health(15.0D, 20.0D).jump(0.72D, 1.02D)
            .bashDamage(0.6D).bashKnockback(0.8D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .build());

    public static final Holder.Reference<ArchetypeType> WAR = register(BhRegistries.archetypeTypeRegistry(), "war", ArchetypeType.builder()
            .speed(0.2083D, 0.3472D).health(25.0D, 40.0D).jump(0.57D, 0.97D)
            .bashDamage(1.5D).bashKnockback(1.2D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .suppressesRear(true).medkitMultiplier(2)
            .build());

    public static final Holder.Reference<ArchetypeType> WESTERN = register(BhRegistries.archetypeTypeRegistry(), "western", ArchetypeType.builder()
            .speed(0.1852D, 0.3472D).health(15.0D, 30.0D).jump(0.57D, 0.84D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .pathSpeedBonus(0.10D, 0.20D, 0.50D)
            .build());

    public static final Holder.Reference<ArchetypeType> DRAFT = register(BhRegistries.archetypeTypeRegistry(), "draft", ArchetypeType.builder()
            .speed(0.1620D, 0.2315D).health(35.0D, 50.0D).jump(0.38D, 0.72D)
            .bashDamage(1.25D).bashKnockback(2.0D)
            .defaultChestRows(4).allowsChestAndRiders(true)
            .baseSpookChance(0.05D).knockbackResistance(0.6D).allowsLargeCart(true)
            .build());

    public static final Holder.Reference<ArchetypeType> PONY = register(BhRegistries.archetypeTypeRegistry(), "pony", ArchetypeType.builder()
            .speed(0.1852D, 0.3009D).health(25.0D, 40.0D).jump(0.57D, 0.84D)
            .bashDamage(0.75D).bashKnockback(0.8D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .passiveHealInterval(400).walksOnPowderSnow(true)
            .fallDamageWaiver(15.0D).stepHeight(2.0D)
            .build());

    public static final Holder.Reference<ArchetypeType> NONE = register(BhRegistries.archetypeTypeRegistry(), "none", ArchetypeType.builder()
            .speed(0.1125D, 0.3375D).health(15.0D, 30.0D).jump(0.40D, 1.00D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .baseSpookChance(0.0D)
            .build());

    public static final Holder.Reference<AbilityType> TOP_END =
            register(BhRegistries.abilityTypeRegistry(), "top_end", new AbilityType(TopEnd::new, true));
    public static final Holder.Reference<AbilityType> ENDURANCE =
            register(BhRegistries.abilityTypeRegistry(), "endurance", new AbilityType(Endurance::new, true));
    public static final Holder.Reference<AbilityType> STANDSTILL_BURST =
            register(BhRegistries.abilityTypeRegistry(), "standstill_burst", new AbilityType(StandstillBurst::new, true));
    public static final Holder.Reference<AbilityType> FRIESIAN_PRESENCE =
            register(BhRegistries.abilityTypeRegistry(), "friesian_presence", new AbilityType(FriesianPresence::new, true));
    public static final Holder.Reference<AbilityType> SECOND_CHANCE =
            register(BhRegistries.abilityTypeRegistry(), "second_chance", new AbilityType(SecondChance::new, true));
    public static final Holder.Reference<AbilityType> SLOW_BLOCK_IMMUNITY =
            register(BhRegistries.abilityTypeRegistry(), "slow_block_immunity", new AbilityType(SlowBlockImmunity::new, true));
    public static final Holder.Reference<AbilityType> IRONCLAD =
            register(BhRegistries.abilityTypeRegistry(), "ironclad", new AbilityType(Ironclad::new, true));
    public static final Holder.Reference<AbilityType> INTIMIDATION =
            register(BhRegistries.abilityTypeRegistry(), "intimidation", new AbilityType(Intimidation::new, true));
    public static final Holder.Reference<AbilityType> BRICK_BREAK =
            register(BhRegistries.abilityTypeRegistry(), "brick_break", new AbilityType(BrickBreak::new, true));
    public static final Holder.Reference<AbilityType> HARDY_NORTHERN =
            register(BhRegistries.abilityTypeRegistry(), "hardy_northern", new AbilityType(HardyNorthern::new, true));
    public static final Holder.Reference<AbilityType> WILD_INSTINCTS =
            register(BhRegistries.abilityTypeRegistry(), "wild_instincts", new AbilityType(WildInstincts::new, true));
    public static final Holder.Reference<AbilityType> HEARTHLIGHT =
            register(BhRegistries.abilityTypeRegistry(), "hearthlight", new AbilityType(Hearthlight::new, true));
    public static final Holder.Reference<AbilityType> EASY_KEEPER =
            register(BhRegistries.abilityTypeRegistry(), "easy_keeper", new AbilityType(EasyKeeper::new, true));
    public static final Holder.Reference<AbilityType> STOCK_HORSE =
            register(BhRegistries.abilityTypeRegistry(), "stock_horse", new AbilityType(StockHorse::new, true));

    public static final Holder.Reference<BreedType> THOROUGHBRED = register(BhRegistries.breedTypeRegistry(), "thoroughbred",
            BreedType.builder(RACE.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.THOROUGHBRED.folder(), BhBreedCoats.THOROUGHBRED.coatIds(), BhBreedCoats.THOROUGHBRED.hasFoalVariant())
                    .entityType(entity("thoroughbred_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(TOP_END.key())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final Holder.Reference<BreedType> ARABIAN = register(BhRegistries.breedTypeRegistry(), "arabian",
            BreedType.builder(RACE.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.ARABIAN.folder(), BhBreedCoats.ARABIAN.coatIds(), BhBreedCoats.ARABIAN.hasFoalVariant())
                    .entityType(entity("arabian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(ENDURANCE.key())
                    .stabilizerBody(StabilizerBody.SMALL)
                    .build());

    public static final Holder.Reference<BreedType> QUARTER = register(BhRegistries.breedTypeRegistry(), "quarter",
            BreedType.builder(RACE.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.QUARTER.folder(), BhBreedCoats.QUARTER.coatIds(), BhBreedCoats.QUARTER.hasFoalVariant())
                    .entityType(entity("quarter_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(STANDSTILL_BURST.key())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final Holder.Reference<BreedType> FRIESIAN = register(BhRegistries.breedTypeRegistry(), "friesian",
            BreedType.builder(WAR.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.FRIESIAN.folder(), BhBreedCoats.FRIESIAN.coatIds(), BhBreedCoats.FRIESIAN.hasFoalVariant())
                    .entityType(entity("friesian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(FRIESIAN_PRESENCE.key())
                    .stabilizerBody(StabilizerBody.FRIESIAN)
                    .build());

    public static final Holder.Reference<BreedType> ANDALUSIAN = register(BhRegistries.breedTypeRegistry(), "andalusian",
            BreedType.builder(WAR.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.ANDALUSIAN.folder(), BhBreedCoats.ANDALUSIAN.coatIds(), BhBreedCoats.ANDALUSIAN.hasFoalVariant())
                    .entityType(entity("andalusian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(SECOND_CHANCE.key())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final Holder.Reference<BreedType> PERCHERON = register(BhRegistries.breedTypeRegistry(), "percheron",
            BreedType.builder(DRAFT.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.PERCHERON.folder(), BhBreedCoats.PERCHERON.coatIds(), BhBreedCoats.PERCHERON.hasFoalVariant())
                    .entityType(entity("percheron_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(SLOW_BLOCK_IMMUNITY.key())
                    .stabilizerBody(StabilizerBody.PERCHERON)
                    .build());

    public static final Holder.Reference<BreedType> CLYDESDALE = register(BhRegistries.breedTypeRegistry(), "clydesdale",
            BreedType.builder(DRAFT.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.CLYDESDALE.folder(), BhBreedCoats.CLYDESDALE.coatIds(), BhBreedCoats.CLYDESDALE.hasFoalVariant())
                    .entityType(entity("clydesdale_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(IRONCLAD.key())
                    .stabilizerBody(StabilizerBody.PERCHERON)
                    .build());

    public static final Holder.Reference<BreedType> SHIRE = register(BhRegistries.breedTypeRegistry(), "shire",
            BreedType.builder(DRAFT.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.SHIRE.folder(), BhBreedCoats.SHIRE.coatIds(), BhBreedCoats.SHIRE.hasFoalVariant())
                    .entityType(entity("shire_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(INTIMIDATION.key())
                    .stabilizerBody(StabilizerBody.SHIRE)
                    .build());

    public static final Holder.Reference<BreedType> BELGIAN = register(BhRegistries.breedTypeRegistry(), "belgian",
            BreedType.builder(DRAFT.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.BELGIAN.folder(), BhBreedCoats.BELGIAN.coatIds(), BhBreedCoats.BELGIAN.hasFoalVariant())
                    .entityType(entity("belgian_horse"))
                    .chestRows(6).bondedChestRows(6)
                    .ability(BRICK_BREAK.key())
                    .stabilizerBody(StabilizerBody.BELGIAN)
                    .build());

    public static final Holder.Reference<BreedType> ICELANDIC = register(BhRegistries.breedTypeRegistry(), "icelandic",
            BreedType.builder(PONY.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.ICELANDIC.folder(), BhBreedCoats.ICELANDIC.coatIds(), BhBreedCoats.ICELANDIC.hasFoalVariant())
                    .entityType(entity("icelandic_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(HARDY_NORTHERN.key())
                    .stabilizerBody(StabilizerBody.ICELANDIC)
                    .build());

    public static final Holder.Reference<BreedType> MUSTANG = register(BhRegistries.breedTypeRegistry(), "mustang",
            BreedType.builder(WAR.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.MUSTANG.folder(), BhBreedCoats.MUSTANG.coatIds(), BhBreedCoats.MUSTANG.hasFoalVariant())
                    .entityType(entity("mustang_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(WILD_INSTINCTS.key())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final Holder.Reference<BreedType> HAFLINGER = register(BhRegistries.breedTypeRegistry(), "haflinger",
            BreedType.builder(PONY.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.HAFLINGER.folder(), BhBreedCoats.HAFLINGER.coatIds(), BhBreedCoats.HAFLINGER.hasFoalVariant())
                    .entityType(entity("haflinger_horse"))
                    .chestRows(4).bondedChestRows(6)
                    .ability(HEARTHLIGHT.key())
                    .stabilizerBody(StabilizerBody.HAFLINGER)
                    .build());

    public static final Holder.Reference<BreedType> MORGAN = register(BhRegistries.breedTypeRegistry(), "morgan",
            BreedType.builder(WESTERN.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.MORGAN.folder(), BhBreedCoats.MORGAN.coatIds(), BhBreedCoats.MORGAN.hasFoalVariant())
                    .entityType(entity("morgan_horse"))
                    .chestRows(3).bondedChestRows(4)
                    .ability(EASY_KEEPER.key())
                    .stabilizerBody(StabilizerBody.SMALL)
                    .build());

    public static final Holder.Reference<BreedType> AMERICAN_PAINT = register(BhRegistries.breedTypeRegistry(), "american_paint",
            BreedType.builder(WESTERN.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.AMERICAN_PAINT.folder(), BhBreedCoats.AMERICAN_PAINT.coatIds(), BhBreedCoats.AMERICAN_PAINT.hasFoalVariant())
                    .entityType(entity("american_paint_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final Holder.Reference<BreedType> APPALOOSA = register(BhRegistries.breedTypeRegistry(), "appaloosa",
            BreedType.builder(WESTERN.key())
                    .coats(IcysBetterHorses.MOD_ID, BhBreedCoats.APPALOOSA.folder(), BhBreedCoats.APPALOOSA.coatIds(), BhBreedCoats.APPALOOSA.hasFoalVariant())
                    .entityType(entity("appaloosa_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(STOCK_HORSE.key())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final Holder.Reference<GenderType> MALE = register(BhRegistries.genderTypeRegistry(), "male", new GenderType());
    public static final Holder.Reference<GenderType> FEMALE = register(BhRegistries.genderTypeRegistry(), "female", new GenderType());

    public static final Holder.Reference<SpeciesType> SPECIES_NONE = register(BhRegistries.speciesTypeRegistry(), "none", new SpeciesType());
    public static final Holder.Reference<SpeciesType> SPECIES_DONKEY = register(BhRegistries.speciesTypeRegistry(), "donkey", new SpeciesType());
    public static final Holder.Reference<SpeciesType> SPECIES_MULE = register(BhRegistries.speciesTypeRegistry(), "mule", new SpeciesType());
    public static final Holder.Reference<SpeciesType> SPECIES_SKELETON = register(BhRegistries.speciesTypeRegistry(), "skeleton", new SpeciesType());
    public static final Holder.Reference<SpeciesType> SPECIES_ZOMBIE = register(BhRegistries.speciesTypeRegistry(), "zombie", new SpeciesType());

    public static final Holder.Reference<CommandType> COMMAND_FOLLOW = register(BhRegistries.commandTypeRegistry(), "follow", new CommandType());
    public static final Holder.Reference<CommandType> COMMAND_STAY = register(BhRegistries.commandTypeRegistry(), "stay", new CommandType());
    public static final Holder.Reference<CommandType> COMMAND_RETURN_HOME = register(BhRegistries.commandTypeRegistry(), "return_home", new CommandType());
    public static final Holder.Reference<CommandType> COMMAND_SET_HOME = register(BhRegistries.commandTypeRegistry(), "set_home", new CommandType());
    public static final Holder.Reference<CommandType> COMMAND_WANDER = register(BhRegistries.commandTypeRegistry(), "wander", new CommandType());
    public static final Holder.Reference<CommandType> COMMAND_ABILITY = register(BhRegistries.commandTypeRegistry(), "ability", new CommandType());

    private BhContent() {
    }

    private static ResourceKey<EntityType<?>> entity(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path));
    }

    public static void init() {
    }

    private static <T> Holder.Reference<T> register(Registry<T> registry, String path, T value) {
        return Registry.registerForHolder(registry, Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path), value);
    }

    public static void logSummary() {
        IcysBetterHorses.LOGGER.info("[content] {} archetypes, {} abilities, {} breeds registered",
                BhRegistries.archetypeTypeRegistry().size(), BhRegistries.abilityTypeRegistry().size(),
                BhRegistries.breedTypeRegistry().size());
    }
}
