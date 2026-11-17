package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.feature.breed.DrownedRider;
import icy.betterhorses.net.feature.breed.RottenRations;
import icy.betterhorses.net.feature.breed.DrownedLungs;
import icy.betterhorses.net.feature.breed.FrostCharge;
import icy.betterhorses.net.feature.breed.SunBaked;
import icy.betterhorses.net.feature.breed.SculkHush;
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
import icy.betterhorses.net.feature.breed.Skittish;
import icy.betterhorses.net.feature.breed.FieldHand;
import icy.betterhorses.net.feature.breed.CoastalScout;
import icy.betterhorses.net.feature.breed.MammothWorker;
import icy.betterhorses.net.feature.breed.MiniatureCompanion;
import icy.betterhorses.net.feature.breed.PegaCompanion;
import icy.betterhorses.net.feature.breed.ThickHide;
import icy.betterhorses.net.feature.breed.WinterFeet;
import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import icy.betterhorses.net.feature.breed.StandstillBurst;
import icy.betterhorses.net.feature.breed.StockHorse;
import icy.betterhorses.net.feature.breed.TopEnd;
import icy.betterhorses.net.feature.breed.WildInstincts;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import java.util.List;

public final class BhContent {

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

    public static final RegistryObject<ArchetypeType> DONKEY = ARCHETYPES.register("donkey", () -> ArchetypeType.builder()
            .speed(0.1852D, 0.2778D).health(35.0D, 50.0D).jump(0.545D, 0.803D)
            .bashDamage(1.5D).bashKnockback(1.0D)
            .defaultChestRows(4).allowsChestAndRiders(false)
            .baseSpookChance(0.0D).stepHeight(2.0D).allowsLargeCart(true).armor(-0.25D)
            .build());

    public static final RegistryObject<ArchetypeType> MULE = ARCHETYPES.register("mule", () -> ArchetypeType.builder()
            .speed(0.1620D, 0.3472D).health(15.0D, 50.0D).jump(0.38D, 1.02D)
            .bashDamage(1.25D).bashKnockback(1.0D)
            .defaultChestRows(4).allowsChestAndRiders(false)
            .fallDamageWaiver(8.0D)
            .build());

    public static final RegistryObject<ArchetypeType> NONE = ARCHETYPES.register("none", () -> ArchetypeType.builder()
            .speed(0.1125D, 0.3375D).health(15.0D, 30.0D).jump(0.40D, 1.00D)
            .bashDamage(1.0D).bashKnockback(1.0D)
            .defaultChestRows(3).allowsChestAndRiders(false)
            .baseSpookChance(0.0D)
            .build());

    public static final RegistryObject<ArchetypeType> SKELETON = ARCHETYPES.register("skeleton", () -> ArchetypeType.builder()
            .speed(10.0D / 43.2D, 17.0D / 43.2D).health(15.0D, 20.0D)
            .jump(BhJumpHeight.strength(3.0D), BhJumpHeight.strength(5.0D))
            .bashDamage(0.75D).bashKnockback(1.0D).defaultChestRows(3)
            .baseSpookChance(0.0D).build());

    public static final RegistryObject<ArchetypeType> ZOMBIE = ARCHETYPES.register("zombie", () -> ArchetypeType.builder()
            .speed(8.0D / 43.2D, 11.0D / 43.2D).health(39.0D, 54.0D)
            .jump(BhJumpHeight.strength(2.0D), BhJumpHeight.strength(4.0D))
            .bashDamage(1.5D).bashKnockback(1.0D).defaultChestRows(3)
            .baseSpookChance(0.0D).build());

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
    public static final RegistryObject<AbilityType> SKITTISH =
            ABILITIES.register("skittish", () -> new AbilityType(Skittish::new, true));
    public static final RegistryObject<AbilityType> FIELD_HAND =
            ABILITIES.register("field_hand", () -> new AbilityType(FieldHand::new, true));
    public static final RegistryObject<AbilityType> COASTAL_SCOUT =
            ABILITIES.register("coastal_scout", () -> new AbilityType(CoastalScout::new, true));
    public static final RegistryObject<AbilityType> MAMMOTH_WORKER =
            ABILITIES.register("mammoth_worker", () -> new AbilityType(MammothWorker::new, true));
    public static final RegistryObject<AbilityType> FARM_COMPANION =
            ABILITIES.register("farm_companion", () -> new AbilityType(MiniatureCompanion::new, true));
    public static final RegistryObject<AbilityType> PEGA_COMPANION =
            ABILITIES.register("pega_companion", () -> new AbilityType(PegaCompanion::new, true));
    public static final RegistryObject<AbilityType> THICK_HIDE =
            ABILITIES.register("thick_hide", () -> new AbilityType(ThickHide::new, true));
    public static final RegistryObject<AbilityType> WINTER_FEET =
            ABILITIES.register("winter_feet", () -> new AbilityType(WinterFeet::new, true));
    public static final RegistryObject<AbilityType> DROWNED_RIDER =
            ABILITIES.register("drowned_rider", () -> new AbilityType(DrownedRider::new, true));
    public static final RegistryObject<AbilityType> ROTTEN_RATIONS =
            ABILITIES.register("rotten_rations", () -> new AbilityType(RottenRations::new, true));
    public static final RegistryObject<AbilityType> FROST_CHARGE =
            ABILITIES.register("frost_charge", () -> new AbilityType(FrostCharge::new, true));
    public static final RegistryObject<AbilityType> SCULK_HUSH =
            ABILITIES.register("sculk_hush", () -> new AbilityType(SculkHush::new, true));
    public static final RegistryObject<AbilityType> SUN_BAKED =
            ABILITIES.register("sun_baked", () -> new AbilityType(SunBaked::new, true));
    public static final RegistryObject<AbilityType> DROWNED_LUNGS =
            ABILITIES.register("drowned_lungs", () -> new AbilityType(DrownedLungs::new, true));

    public static final DeferredRegister<BreedType> BREEDS =
            DeferredRegister.create(BhRegistries.BREED_TYPES, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<BreedType> THOROUGHBRED = BREEDS.register("thoroughbred", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.THOROUGHBRED.folder(), BhBreedCoats.THOROUGHBRED.coatIds(), BhBreedCoats.THOROUGHBRED.hasFoalVariant())
                    .entityType(entity("thoroughbred_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(TOP_END.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final RegistryObject<BreedType> ARABIAN = BREEDS.register("arabian", () ->
            BreedType.builder(RACE.getKey()).small()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ARABIAN.folder(), BhBreedCoats.ARABIAN.coatIds(), BhBreedCoats.ARABIAN.hasFoalVariant())
                    .entityType(entity("arabian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(ENDURANCE.getKey())
                    .stabilizerBody(StabilizerBody.SMALL)
                    .build());

    public static final RegistryObject<BreedType> QUARTER = BREEDS.register("quarter", () ->
            BreedType.builder(RACE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.QUARTER.folder(), BhBreedCoats.QUARTER.coatIds(), BhBreedCoats.QUARTER.hasFoalVariant())
                    .entityType(entity("quarter_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(STANDSTILL_BURST.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final RegistryObject<BreedType> FRIESIAN = BREEDS.register("friesian", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.FRIESIAN.folder(), BhBreedCoats.FRIESIAN.coatIds(), BhBreedCoats.FRIESIAN.hasFoalVariant())
                    .entityType(entity("friesian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(FRIESIAN_PRESENCE.getKey())
                    .stabilizerBody(StabilizerBody.FRIESIAN)
                    .build());

    public static final RegistryObject<BreedType> ANDALUSIAN = BREEDS.register("andalusian", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ANDALUSIAN.folder(), BhBreedCoats.ANDALUSIAN.coatIds(), BhBreedCoats.ANDALUSIAN.hasFoalVariant())
                    .entityType(entity("andalusian_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(SECOND_CHANCE.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final RegistryObject<BreedType> PERCHERON = BREEDS.register("percheron", () ->
            BreedType.builder(DRAFT.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.PERCHERON.folder(), BhBreedCoats.PERCHERON.coatIds(), BhBreedCoats.PERCHERON.hasFoalVariant())
                    .entityType(entity("percheron_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(SLOW_BLOCK_IMMUNITY.getKey())
                    .stabilizerBody(StabilizerBody.PERCHERON)
                    .build());

    public static final RegistryObject<BreedType> CLYDESDALE = BREEDS.register("clydesdale", () ->
            BreedType.builder(DRAFT.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.CLYDESDALE.folder(), BhBreedCoats.CLYDESDALE.coatIds(), BhBreedCoats.CLYDESDALE.hasFoalVariant())
                    .entityType(entity("clydesdale_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(IRONCLAD.getKey())
                    .stabilizerBody(StabilizerBody.PERCHERON)
                    .build());

    public static final RegistryObject<BreedType> SHIRE = BREEDS.register("shire", () ->
            BreedType.builder(DRAFT.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.SHIRE.folder(), BhBreedCoats.SHIRE.coatIds(), BhBreedCoats.SHIRE.hasFoalVariant())
                    .entityType(entity("shire_horse"))
                    .chestRows(4).bondedChestRows(4)
                    .ability(INTIMIDATION.getKey())
                    .stabilizerBody(StabilizerBody.SHIRE)
                    .build());

    public static final RegistryObject<BreedType> BELGIAN = BREEDS.register("belgian", () ->
            BreedType.builder(DRAFT.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.BELGIAN.folder(), BhBreedCoats.BELGIAN.coatIds(), BhBreedCoats.BELGIAN.hasFoalVariant())
                    .entityType(entity("belgian_horse"))
                    .chestRows(6).bondedChestRows(6)
                    .ability(BRICK_BREAK.getKey())
                    .stabilizerBody(StabilizerBody.BELGIAN)
                    .build());

    public static final RegistryObject<BreedType> ICELANDIC = BREEDS.register("icelandic", () ->
            BreedType.builder(PONY.getKey()).small()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ICELANDIC.folder(), BhBreedCoats.ICELANDIC.coatIds(), BhBreedCoats.ICELANDIC.hasFoalVariant())
                    .entityType(entity("icelandic_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(HARDY_NORTHERN.getKey())
                    .stabilizerBody(StabilizerBody.ICELANDIC)
                    .build());

    public static final RegistryObject<BreedType> MUSTANG = BREEDS.register("mustang", () ->
            BreedType.builder(WAR.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MUSTANG.folder(), BhBreedCoats.MUSTANG.coatIds(), BhBreedCoats.MUSTANG.hasFoalVariant())
                    .entityType(entity("mustang_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(WILD_INSTINCTS.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final RegistryObject<BreedType> HAFLINGER = BREEDS.register("haflinger", () ->
            BreedType.builder(PONY.getKey()).small()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.HAFLINGER.folder(), BhBreedCoats.HAFLINGER.coatIds(), BhBreedCoats.HAFLINGER.hasFoalVariant())
                    .entityType(entity("haflinger_horse"))
                    .chestRows(4).bondedChestRows(6)
                    .ability(HEARTHLIGHT.getKey())
                    .stabilizerBody(StabilizerBody.HAFLINGER)
                    .build());

    public static final RegistryObject<BreedType> MORGAN = BREEDS.register("morgan", () ->
            BreedType.builder(WESTERN.getKey()).small()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MORGAN.folder(), BhBreedCoats.MORGAN.coatIds(), BhBreedCoats.MORGAN.hasFoalVariant())
                    .entityType(entity("morgan_horse"))
                    .chestRows(3).bondedChestRows(4)
                    .ability(EASY_KEEPER.getKey())
                    .stabilizerBody(StabilizerBody.SMALL)
                    .build());

    public static final RegistryObject<BreedType> AMERICAN_PAINT = BREEDS.register("american_paint", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.AMERICAN_PAINT.folder(), BhBreedCoats.AMERICAN_PAINT.coatIds(), BhBreedCoats.AMERICAN_PAINT.hasFoalVariant())
                    .entityType(entity("american_paint_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final RegistryObject<BreedType> APPALOOSA = BREEDS.register("appaloosa", () ->
            BreedType.builder(WESTERN.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.APPALOOSA.folder(), BhBreedCoats.APPALOOSA.coatIds(), BhBreedCoats.APPALOOSA.hasFoalVariant())
                    .entityType(entity("appaloosa_horse"))
                    .chestRows(3).bondedChestRows(3)
                    .ability(STOCK_HORSE.getKey())
                    .stabilizerBody(StabilizerBody.MEDIUM)
                    .build());

    public static final RegistryObject<BreedType> ANDALUSIAN_DONKEY = BREEDS.register("andalusian_donkey", () ->
            BreedType.builder(DONKEY.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ANDALUSIAN_DONKEY.folder(), BhBreedCoats.ANDALUSIAN_DONKEY.coatIds(), BhBreedCoats.ANDALUSIAN_DONKEY.hasFoalVariant())
                    .entityType(entity("andalusian_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(FIELD_HAND.getKey())
                    .stabilizerBody(StabilizerBody.ANDALUSIAN_DONKEY)
                    .build());

    public static final RegistryObject<BreedType> ASINARA_DONKEY = BREEDS.register("asinara_donkey", () ->
            BreedType.builder(DONKEY.getKey()).small()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ASINARA_DONKEY.folder(), BhBreedCoats.ASINARA_DONKEY.coatIds(), BhBreedCoats.ASINARA_DONKEY.hasFoalVariant())
                    .entityType(entity("asinara_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(COASTAL_SCOUT.getKey())
                    .stabilizerBody(StabilizerBody.ASINARA_DONKEY)
                    .build());

    public static final RegistryObject<BreedType> MAMMOTH_DONKEY = BREEDS.register("mammoth_donkey", () ->
            BreedType.builder(DONKEY.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MAMMOTH_DONKEY.folder(), BhBreedCoats.MAMMOTH_DONKEY.coatIds(), BhBreedCoats.MAMMOTH_DONKEY.hasFoalVariant())
                    .entityType(entity("mammoth_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(MAMMOTH_WORKER.getKey())
                    .stabilizerBody(StabilizerBody.MAMMOTH_DONKEY)
                    .build());

    public static final RegistryObject<BreedType> MINIATURE_DONKEY = BREEDS.register("miniature_donkey", () ->
            BreedType.builder(DONKEY.getKey()).small()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.MINIATURE_DONKEY.folder(), BhBreedCoats.MINIATURE_DONKEY.coatIds(), BhBreedCoats.MINIATURE_DONKEY.hasFoalVariant())
                    .entityType(entity("miniature_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(FARM_COMPANION.getKey())
                    .ability(SKITTISH.getKey())
                    .stabilizerBody(StabilizerBody.MINIATURE_DONKEY)
                    .noCarts()
                    .build());

    public static final RegistryObject<BreedType> PEGA_DONKEY = BREEDS.register("pega_donkey", () ->
            BreedType.builder(DONKEY.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.PEGA_DONKEY.folder(), BhBreedCoats.PEGA_DONKEY.coatIds(), BhBreedCoats.PEGA_DONKEY.hasFoalVariant())
                    .entityType(entity("pega_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(PEGA_COMPANION.getKey())
                    .stabilizerBody(StabilizerBody.PEGA_DONKEY)
                    .build());

    public static final RegistryObject<BreedType> POITOU_DONKEY = BREEDS.register("poitou_donkey", () ->
            BreedType.builder(DONKEY.getKey()).large()
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.POITOU_DONKEY.folder(), BhBreedCoats.POITOU_DONKEY.coatIds(), BhBreedCoats.POITOU_DONKEY.hasFoalVariant())
                    .entityType(entity("poitou_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(THICK_HIDE.getKey())
                    .stabilizerBody(StabilizerBody.POITOU_DONKEY)
                    .build());

    public static final RegistryObject<BreedType> ZAMORANO_DONKEY = BREEDS.register("zamorano_donkey", () ->
            BreedType.builder(DONKEY.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, BhBreedCoats.ZAMORANO_DONKEY.folder(), BhBreedCoats.ZAMORANO_DONKEY.coatIds(), BhBreedCoats.ZAMORANO_DONKEY.hasFoalVariant())
                    .entityType(entity("zamorano_donkey"))
                    .chestRows(4).midChestRows(5).bondedChestRows(6)
                    .ability(WINTER_FEET.getKey())
                    .stabilizerBody(StabilizerBody.ZAMORANO_DONKEY)
                    .build());

    public static final RegistryObject<BreedType> LARGE_SKELETON_HORSE =
            undead("large_skeleton_horse", SKELETON, DROWNED_RIDER, StabilizerBody.PERCHERON);
    public static final RegistryObject<BreedType> MEDIUM_SKELETON_HORSE =
            undead("medium_skeleton_horse", SKELETON, DROWNED_RIDER, StabilizerBody.MEDIUM);
    public static final RegistryObject<BreedType> SMALL_SKELETON_HORSE =
            undead("small_skeleton_horse", SKELETON, DROWNED_RIDER, StabilizerBody.SMALL);
    public static final RegistryObject<BreedType> LARGE_ZOMBIE_HORSE =
            undead("large_zombie_horse", ZOMBIE, ROTTEN_RATIONS, StabilizerBody.PERCHERON);
    public static final RegistryObject<BreedType> MEDIUM_ZOMBIE_HORSE =
            undead("medium_zombie_horse", ZOMBIE, ROTTEN_RATIONS, StabilizerBody.MEDIUM);
    public static final RegistryObject<BreedType> SMALL_ZOMBIE_HORSE =
            undead("small_zombie_horse", ZOMBIE, ROTTEN_RATIONS, StabilizerBody.SMALL);
    public static final RegistryObject<BreedType> LARGE_STRAY_HORSE =
            undead("large_stray_horse", SKELETON, DROWNED_RIDER, FROST_CHARGE, StabilizerBody.PERCHERON);
    public static final RegistryObject<BreedType> MEDIUM_STRAY_HORSE =
            undead("medium_stray_horse", SKELETON, DROWNED_RIDER, FROST_CHARGE, StabilizerBody.MEDIUM);
    public static final RegistryObject<BreedType> SMALL_STRAY_HORSE =
            undead("small_stray_horse", SKELETON, DROWNED_RIDER, FROST_CHARGE, StabilizerBody.SMALL);
    public static final RegistryObject<BreedType> LARGE_SCULK_HORSE =
            undead("large_sculk_horse", SKELETON, DROWNED_RIDER, SCULK_HUSH, StabilizerBody.PERCHERON);
    public static final RegistryObject<BreedType> MEDIUM_SCULK_HORSE =
            undead("medium_sculk_horse", SKELETON, DROWNED_RIDER, SCULK_HUSH, StabilizerBody.MEDIUM);
    public static final RegistryObject<BreedType> SMALL_SCULK_HORSE =
            undead("small_sculk_horse", SKELETON, DROWNED_RIDER, SCULK_HUSH, StabilizerBody.SMALL);
    public static final RegistryObject<BreedType> LARGE_HUSK_HORSE =
            undead("large_husk_horse", ZOMBIE, ROTTEN_RATIONS, SUN_BAKED, StabilizerBody.PERCHERON);
    public static final RegistryObject<BreedType> MEDIUM_HUSK_HORSE =
            undead("medium_husk_horse", ZOMBIE, ROTTEN_RATIONS, SUN_BAKED, StabilizerBody.MEDIUM);
    public static final RegistryObject<BreedType> SMALL_HUSK_HORSE =
            undead("small_husk_horse", ZOMBIE, ROTTEN_RATIONS, SUN_BAKED, StabilizerBody.SMALL);
    public static final RegistryObject<BreedType> LARGE_DROWNED_HORSE =
            undead("large_drowned_horse", ZOMBIE, ROTTEN_RATIONS, DROWNED_LUNGS, StabilizerBody.PERCHERON);
    public static final RegistryObject<BreedType> MEDIUM_DROWNED_HORSE =
            undead("medium_drowned_horse", ZOMBIE, ROTTEN_RATIONS, DROWNED_LUNGS, StabilizerBody.MEDIUM);
    public static final RegistryObject<BreedType> SMALL_DROWNED_HORSE =
            undead("small_drowned_horse", ZOMBIE, ROTTEN_RATIONS, DROWNED_LUNGS, StabilizerBody.SMALL);

    public static final RegistryObject<BreedType> SMALL_MULE = mule("small_mule", "small_mule",
            BreedType.SMALL, StabilizerBody.SMALL_MULE);
    public static final RegistryObject<BreedType> MEDIUM_MULE = mule("medium_mule", "mule",
            BreedType.MEDIUM, StabilizerBody.MULE);
    public static final RegistryObject<BreedType> LARGE_MULE = mule("large_mule", "large_mule",
            BreedType.LARGE, StabilizerBody.LARGE_MULE);

    private static final List<String> MULE_COATS = List.of("brown", "dark_brown", "sorrel", "black", "grey",
            "palomino", "buckskin", "grullo", "flaxen_sorrel", "appaloosa", "pinto", "roan");

    private static RegistryObject<BreedType> mule(String path, String folder, int size, StabilizerBody body) {
        return BREEDS.register(path, () -> {
            BreedType.Builder b = BreedType.builder(MULE.getKey())
                    .coats(IcysBetterHorses.RESOURCE_NAMESPACE, folder, MULE_COATS, true)
                    .entityType(entity(path))
                    .stabilizerBody(body);
            if (size == BreedType.SMALL) b.small();
            if (size == BreedType.LARGE) b.large();
            return b.build();
        });
    }

    private static RegistryObject<BreedType> undead(String path,
            RegistryObject<ArchetypeType> archetype,
            RegistryObject<AbilityType> ability, StabilizerBody body) {
        return BREEDS.register(path, () -> BreedType.builder(archetype.getKey())
                .coats(IcysBetterHorses.RESOURCE_NAMESPACE, path, List.of("default"), false)
                .entityType(entity(path)).ability(ability.getKey()).stabilizerBody(body).build());
    }

    private static RegistryObject<BreedType> undead(String path,
            RegistryObject<ArchetypeType> archetype,
            RegistryObject<AbilityType> ability,
            RegistryObject<AbilityType> variant, StabilizerBody body) {
        return BREEDS.register(path, () -> BreedType.builder(archetype.getKey())
                .coats(IcysBetterHorses.RESOURCE_NAMESPACE, path, List.of("default"), false)
                .entityType(entity(path)).ability(ability.getKey()).ability(variant.getKey())
                .stabilizerBody(body).build());
    }

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

    private static ResourceKey<EntityType<?>> entity(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
                new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path));
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
