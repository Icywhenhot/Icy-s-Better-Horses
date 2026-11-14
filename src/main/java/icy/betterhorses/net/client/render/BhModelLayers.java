package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.function.Function;

public final class BhModelLayers {

    public static final ModelLayerLocation LARGE_UNDEAD_HORSE = layer("large_undead_horse", "main");
    public static final ModelLayerLocation MEDIUM_UNDEAD_HORSE = layer("medium_undead_horse", "main");
    public static final ModelLayerLocation SMALL_UNDEAD_HORSE = layer("small_undead_horse", "main");

    public static final ModelLayerLocation ICELANDIC_HORSE = layer("icelandic_horse", "main");
    public static final ModelLayerLocation ICELANDIC_HORSE_BABY = layer("icelandic_horse", "baby");
    public static final ModelLayerLocation ICELANDIC_SADDLE = layer("icelandic_horse", "saddle");
    public static final ModelLayerLocation ICELANDIC_SADDLE_BABY = layer("icelandic_horse", "saddle_baby");
    public static final ModelLayerLocation ICELANDIC_CARPET = layer("icelandic_horse", "carpet");
    public static final ModelLayerLocation ICELANDIC_CARPET_BABY = layer("icelandic_horse", "carpet_baby");
    public static final ModelLayerLocation ICELANDIC_ARMOR = layer("icelandic_horse", "armor");
    public static final ModelLayerLocation ICELANDIC_ARMOR_BABY = layer("icelandic_horse", "armor_baby");
    public static final ModelLayerLocation ICELANDIC_CHEST = layer("icelandic_horse", "chest");
    public static final ModelLayerLocation ICELANDIC_CHEST_BABY = layer("icelandic_horse", "chest_baby");

    public static final ModelLayerLocation FRIESIAN_HORSE = layer("friesian_horse", "main");
    public static final ModelLayerLocation FRIESIAN_HORSE_BABY = layer("friesian_horse", "baby");
    public static final ModelLayerLocation FRIESIAN_SADDLE = layer("friesian_horse", "saddle");
    public static final ModelLayerLocation FRIESIAN_SADDLE_BABY = layer("friesian_horse", "saddle_baby");
    public static final ModelLayerLocation FRIESIAN_CARPET = layer("friesian_horse", "carpet");
    public static final ModelLayerLocation FRIESIAN_CARPET_BABY = layer("friesian_horse", "carpet_baby");
    public static final ModelLayerLocation FRIESIAN_ARMOR = layer("friesian_horse", "armor");
    public static final ModelLayerLocation FRIESIAN_ARMOR_BABY = layer("friesian_horse", "armor_baby");
    public static final ModelLayerLocation FRIESIAN_CHEST = layer("friesian_horse", "chest");
    public static final ModelLayerLocation FRIESIAN_CHEST_BABY = layer("friesian_horse", "chest_baby");

    public static final ModelLayerLocation SMALL_HORSE = layer("small_horse", "main");
    public static final ModelLayerLocation SMALL_HORSE_BABY = layer("small_horse", "baby");
    public static final ModelLayerLocation SMALL_SADDLE = layer("small_horse", "saddle");
    public static final ModelLayerLocation SMALL_SADDLE_BABY = layer("small_horse", "saddle_baby");
    public static final ModelLayerLocation SMALL_CARPET = layer("small_horse", "carpet");
    public static final ModelLayerLocation SMALL_CARPET_BABY = layer("small_horse", "carpet_baby");
    public static final ModelLayerLocation SMALL_ARMOR = layer("small_horse", "armor");
    public static final ModelLayerLocation SMALL_ARMOR_BABY = layer("small_horse", "armor_baby");
    public static final ModelLayerLocation SMALL_CHEST = layer("small_horse", "chest");
    public static final ModelLayerLocation SMALL_CHEST_BABY = layer("small_horse", "chest_baby");

    public static final ModelLayerLocation HAFLINGER_HORSE = layer("haflinger_horse", "main");
    public static final ModelLayerLocation HAFLINGER_HORSE_BABY = layer("haflinger_horse", "baby");
    public static final ModelLayerLocation HAFLINGER_SADDLE = layer("haflinger_horse", "saddle");
    public static final ModelLayerLocation HAFLINGER_SADDLE_BABY = layer("haflinger_horse", "saddle_baby");
    public static final ModelLayerLocation HAFLINGER_CARPET = layer("haflinger_horse", "carpet");
    public static final ModelLayerLocation HAFLINGER_CARPET_BABY = layer("haflinger_horse", "carpet_baby");
    public static final ModelLayerLocation HAFLINGER_ARMOR = layer("haflinger_horse", "armor");
    public static final ModelLayerLocation HAFLINGER_ARMOR_BABY = layer("haflinger_horse", "armor_baby");
    public static final ModelLayerLocation HAFLINGER_CHEST = layer("haflinger_horse", "chest");
    public static final ModelLayerLocation HAFLINGER_CHEST_BABY = layer("haflinger_horse", "chest_baby");

    public static final ModelLayerLocation MEDIUM_HORSE = layer("medium_horse", "main");
    public static final ModelLayerLocation MEDIUM_HORSE_BABY = layer("medium_horse", "baby");
    public static final ModelLayerLocation MEDIUM_SADDLE = layer("medium_horse", "saddle");
    public static final ModelLayerLocation MEDIUM_SADDLE_BABY = layer("medium_horse", "saddle_baby");
    public static final ModelLayerLocation MEDIUM_CARPET = layer("medium_horse", "carpet");
    public static final ModelLayerLocation MEDIUM_CARPET_BABY = layer("medium_horse", "carpet_baby");
    public static final ModelLayerLocation MEDIUM_ARMOR = layer("medium_horse", "armor");
    public static final ModelLayerLocation MEDIUM_ARMOR_BABY = layer("medium_horse", "armor_baby");
    public static final ModelLayerLocation MEDIUM_CHEST = layer("medium_horse", "chest");
    public static final ModelLayerLocation MEDIUM_CHEST_BABY = layer("medium_horse", "chest_baby");

    public static final ModelLayerLocation PERCHERON_HORSE = layer("percheron_horse", "main");
    public static final ModelLayerLocation PERCHERON_HORSE_BABY = layer("percheron_horse", "baby");
    public static final ModelLayerLocation PERCHERON_SADDLE = layer("percheron_horse", "saddle");
    public static final ModelLayerLocation PERCHERON_SADDLE_BABY = layer("percheron_horse", "saddle_baby");
    public static final ModelLayerLocation PERCHERON_CARPET = layer("percheron_horse", "carpet");
    public static final ModelLayerLocation PERCHERON_CARPET_BABY = layer("percheron_horse", "carpet_baby");
    public static final ModelLayerLocation PERCHERON_ARMOR = layer("percheron_horse", "armor");
    public static final ModelLayerLocation PERCHERON_ARMOR_BABY = layer("percheron_horse", "armor_baby");
    public static final ModelLayerLocation PERCHERON_CHEST = layer("percheron_horse", "chest");
    public static final ModelLayerLocation PERCHERON_CHEST_BABY = layer("percheron_horse", "chest_baby");

    public static final ModelLayerLocation SHIRE_HORSE = layer("shire_horse", "main");
    public static final ModelLayerLocation SHIRE_HORSE_BABY = layer("shire_horse", "baby");
    public static final ModelLayerLocation SHIRE_SADDLE = layer("shire_horse", "saddle");
    public static final ModelLayerLocation SHIRE_SADDLE_BABY = layer("shire_horse", "saddle_baby");
    public static final ModelLayerLocation SHIRE_CARPET = layer("shire_horse", "carpet");
    public static final ModelLayerLocation SHIRE_CARPET_BABY = layer("shire_horse", "carpet_baby");
    public static final ModelLayerLocation SHIRE_ARMOR = layer("shire_horse", "armor");
    public static final ModelLayerLocation SHIRE_ARMOR_BABY = layer("shire_horse", "armor_baby");
    public static final ModelLayerLocation SHIRE_CHEST = layer("shire_horse", "chest");
    public static final ModelLayerLocation SHIRE_CHEST_BABY = layer("shire_horse", "chest_baby");

    public static final ModelLayerLocation BELGIAN_HORSE = layer("belgian_horse", "main");
    public static final ModelLayerLocation BELGIAN_HORSE_BABY = layer("belgian_horse", "baby");
    public static final ModelLayerLocation BELGIAN_SADDLE = layer("belgian_horse", "saddle");
    public static final ModelLayerLocation BELGIAN_SADDLE_BABY = layer("belgian_horse", "saddle_baby");
    public static final ModelLayerLocation BELGIAN_CARPET = layer("belgian_horse", "carpet");
    public static final ModelLayerLocation BELGIAN_CARPET_BABY = layer("belgian_horse", "carpet_baby");
    public static final ModelLayerLocation BELGIAN_ARMOR = layer("belgian_horse", "armor");
    public static final ModelLayerLocation BELGIAN_ARMOR_BABY = layer("belgian_horse", "armor_baby");
    public static final ModelLayerLocation BELGIAN_CHEST = layer("belgian_horse", "chest");
    public static final ModelLayerLocation BELGIAN_CHEST_BABY = layer("belgian_horse", "chest_baby");

    public static final ModelLayerLocation CLYDESDALE_HORSE = layer("clydesdale_horse", "main");
    public static final ModelLayerLocation CLYDESDALE_HORSE_BABY = layer("clydesdale_horse", "baby");

    private BhModelLayers() {}

    public static final ModelLayerLocation ANDALUSIAN_DONKEY = layer("andalusian_donkey", "main");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_BABY = layer("andalusian_donkey", "baby");
    public static final ModelLayerLocation ASINARA_DONKEY = layer("asinara_donkey", "main");
    public static final ModelLayerLocation ASINARA_DONKEY_BABY = layer("asinara_donkey", "baby");
    public static final ModelLayerLocation MAMMOTH_DONKEY = layer("mammoth_donkey", "main");
    public static final ModelLayerLocation MAMMOTH_DONKEY_BABY = layer("mammoth_donkey", "baby");
    public static final ModelLayerLocation MINIATURE_DONKEY = layer("miniature_donkey", "main");
    public static final ModelLayerLocation MINIATURE_DONKEY_BABY = layer("miniature_donkey", "baby");
    public static final ModelLayerLocation PEGA_DONKEY = layer("pega_donkey", "main");
    public static final ModelLayerLocation PEGA_DONKEY_BABY = layer("pega_donkey", "baby");
    public static final ModelLayerLocation POITOU_DONKEY = layer("poitou_donkey", "main");
    public static final ModelLayerLocation POITOU_DONKEY_BABY = layer("poitou_donkey", "baby");
    public static final ModelLayerLocation ZAMORANO_DONKEY = layer("zamorano_donkey", "main");
    public static final ModelLayerLocation ZAMORANO_DONKEY_BABY = layer("zamorano_donkey", "baby");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_SADDLE = layer("andalusian_donkey", "saddle");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_SADDLE_BABY = layer("andalusian_donkey", "saddle_baby");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_CARPET = layer("andalusian_donkey", "carpet");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_CARPET_BABY = layer("andalusian_donkey", "carpet_baby");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_ARMOR = layer("andalusian_donkey", "armor");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_ARMOR_BABY = layer("andalusian_donkey", "armor_baby");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_CHEST = layer("andalusian_donkey", "chest");
    public static final ModelLayerLocation ANDALUSIAN_DONKEY_CHEST_BABY = layer("andalusian_donkey", "chest_baby");
    public static final ModelLayerLocation ASINARA_DONKEY_SADDLE = layer("asinara_donkey", "saddle");
    public static final ModelLayerLocation ASINARA_DONKEY_SADDLE_BABY = layer("asinara_donkey", "saddle_baby");
    public static final ModelLayerLocation ASINARA_DONKEY_CARPET = layer("asinara_donkey", "carpet");
    public static final ModelLayerLocation ASINARA_DONKEY_CARPET_BABY = layer("asinara_donkey", "carpet_baby");
    public static final ModelLayerLocation ASINARA_DONKEY_ARMOR = layer("asinara_donkey", "armor");
    public static final ModelLayerLocation ASINARA_DONKEY_ARMOR_BABY = layer("asinara_donkey", "armor_baby");
    public static final ModelLayerLocation ASINARA_DONKEY_CHEST = layer("asinara_donkey", "chest");
    public static final ModelLayerLocation ASINARA_DONKEY_CHEST_BABY = layer("asinara_donkey", "chest_baby");
    public static final ModelLayerLocation MAMMOTH_DONKEY_SADDLE = layer("mammoth_donkey", "saddle");
    public static final ModelLayerLocation MAMMOTH_DONKEY_SADDLE_BABY = layer("mammoth_donkey", "saddle_baby");
    public static final ModelLayerLocation MAMMOTH_DONKEY_CARPET = layer("mammoth_donkey", "carpet");
    public static final ModelLayerLocation MAMMOTH_DONKEY_CARPET_BABY = layer("mammoth_donkey", "carpet_baby");
    public static final ModelLayerLocation MAMMOTH_DONKEY_ARMOR = layer("mammoth_donkey", "armor");
    public static final ModelLayerLocation MAMMOTH_DONKEY_ARMOR_BABY = layer("mammoth_donkey", "armor_baby");
    public static final ModelLayerLocation MAMMOTH_DONKEY_CHEST = layer("mammoth_donkey", "chest");
    public static final ModelLayerLocation MAMMOTH_DONKEY_CHEST_BABY = layer("mammoth_donkey", "chest_baby");
    public static final ModelLayerLocation MINIATURE_DONKEY_SADDLE = layer("miniature_donkey", "saddle");
    public static final ModelLayerLocation MINIATURE_DONKEY_SADDLE_BABY = layer("miniature_donkey", "saddle_baby");
    public static final ModelLayerLocation MINIATURE_DONKEY_CARPET = layer("miniature_donkey", "carpet");
    public static final ModelLayerLocation MINIATURE_DONKEY_CARPET_BABY = layer("miniature_donkey", "carpet_baby");
    public static final ModelLayerLocation MINIATURE_DONKEY_ARMOR = layer("miniature_donkey", "armor");
    public static final ModelLayerLocation MINIATURE_DONKEY_ARMOR_BABY = layer("miniature_donkey", "armor_baby");
    public static final ModelLayerLocation MINIATURE_DONKEY_CHEST = layer("miniature_donkey", "chest");
    public static final ModelLayerLocation MINIATURE_DONKEY_CHEST_BABY = layer("miniature_donkey", "chest_baby");
    public static final ModelLayerLocation PEGA_DONKEY_SADDLE = layer("pega_donkey", "saddle");
    public static final ModelLayerLocation PEGA_DONKEY_SADDLE_BABY = layer("pega_donkey", "saddle_baby");
    public static final ModelLayerLocation PEGA_DONKEY_CARPET = layer("pega_donkey", "carpet");
    public static final ModelLayerLocation PEGA_DONKEY_CARPET_BABY = layer("pega_donkey", "carpet_baby");
    public static final ModelLayerLocation PEGA_DONKEY_ARMOR = layer("pega_donkey", "armor");
    public static final ModelLayerLocation PEGA_DONKEY_ARMOR_BABY = layer("pega_donkey", "armor_baby");
    public static final ModelLayerLocation PEGA_DONKEY_CHEST = layer("pega_donkey", "chest");
    public static final ModelLayerLocation PEGA_DONKEY_CHEST_BABY = layer("pega_donkey", "chest_baby");
    public static final ModelLayerLocation POITOU_DONKEY_SADDLE = layer("poitou_donkey", "saddle");
    public static final ModelLayerLocation POITOU_DONKEY_SADDLE_BABY = layer("poitou_donkey", "saddle_baby");
    public static final ModelLayerLocation POITOU_DONKEY_CARPET = layer("poitou_donkey", "carpet");
    public static final ModelLayerLocation POITOU_DONKEY_CARPET_BABY = layer("poitou_donkey", "carpet_baby");
    public static final ModelLayerLocation POITOU_DONKEY_ARMOR = layer("poitou_donkey", "armor");
    public static final ModelLayerLocation POITOU_DONKEY_ARMOR_BABY = layer("poitou_donkey", "armor_baby");
    public static final ModelLayerLocation POITOU_DONKEY_CHEST = layer("poitou_donkey", "chest");
    public static final ModelLayerLocation POITOU_DONKEY_CHEST_BABY = layer("poitou_donkey", "chest_baby");
    public static final ModelLayerLocation ZAMORANO_DONKEY_SADDLE = layer("zamorano_donkey", "saddle");
    public static final ModelLayerLocation ZAMORANO_DONKEY_SADDLE_BABY = layer("zamorano_donkey", "saddle_baby");
    public static final ModelLayerLocation ZAMORANO_DONKEY_CARPET = layer("zamorano_donkey", "carpet");
    public static final ModelLayerLocation ZAMORANO_DONKEY_CARPET_BABY = layer("zamorano_donkey", "carpet_baby");
    public static final ModelLayerLocation ZAMORANO_DONKEY_ARMOR = layer("zamorano_donkey", "armor");
    public static final ModelLayerLocation ZAMORANO_DONKEY_ARMOR_BABY = layer("zamorano_donkey", "armor_baby");
    public static final ModelLayerLocation ZAMORANO_DONKEY_CHEST = layer("zamorano_donkey", "chest");
    public static final ModelLayerLocation ZAMORANO_DONKEY_CHEST_BABY = layer("zamorano_donkey", "chest_baby");

    public static final ModelLayerLocation MULE = layer("mule", "main");
    public static final ModelLayerLocation MULE_SADDLE = layer("mule", "saddle");
    public static final ModelLayerLocation MULE_SADDLE_BABY = layer("mule", "saddle_baby");
    public static final ModelLayerLocation MULE_CARPET = layer("mule", "carpet");
    public static final ModelLayerLocation MULE_CARPET_BABY = layer("mule", "carpet_baby");
    public static final ModelLayerLocation MULE_ARMOR = layer("mule", "armor");
    public static final ModelLayerLocation MULE_ARMOR_BABY = layer("mule", "armor_baby");
    public static final ModelLayerLocation MULE_CHEST = layer("mule", "chest");
    public static final ModelLayerLocation MULE_CHEST_BABY = layer("mule", "chest_baby");
    public static final ModelLayerLocation MULE_BABY = layer("mule", "baby");

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LARGE_UNDEAD_HORSE, LargeUndeadHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(MEDIUM_UNDEAD_HORSE, MediumUndeadHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(SMALL_UNDEAD_HORSE, SmallUndeadHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_HORSE, IcelandicHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_HORSE_BABY, IcelandicFoalGeometry::createBodyLayer);

        event.registerLayerDefinition(
                ICELANDIC_SADDLE, IcelandicSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_SADDLE_BABY, IcelandicSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_CARPET, IcelandicCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_CARPET_BABY, IcelandicCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_ARMOR, IcelandicArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_ARMOR_BABY, IcelandicArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_CHEST, IcelandicChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_CHEST_BABY, IcelandicChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                FRIESIAN_HORSE, FriesianHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_HORSE_BABY, FriesianFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_SADDLE, FriesianSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_SADDLE_BABY, FriesianSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_CARPET, FriesianCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_CARPET_BABY, FriesianCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_ARMOR, FriesianArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_ARMOR_BABY, FriesianArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_CHEST, FriesianChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                FRIESIAN_CHEST_BABY, FriesianChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                SMALL_HORSE, SmallHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_HORSE_BABY, SmallFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_SADDLE, SmallSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_SADDLE_BABY, SmallSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_CARPET, SmallCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_CARPET_BABY, SmallCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_ARMOR, SmallArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_ARMOR_BABY, SmallArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_CHEST, SmallChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SMALL_CHEST_BABY, SmallChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                HAFLINGER_HORSE, HaflingerHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_HORSE_BABY, SmallFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_SADDLE, HaflingerSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_SADDLE_BABY, HaflingerSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_CARPET, HaflingerCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_CARPET_BABY, HaflingerCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_ARMOR, HaflingerArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_ARMOR_BABY, HaflingerArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_CHEST, HaflingerChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                HAFLINGER_CHEST_BABY, HaflingerChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                MEDIUM_HORSE, MediumHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_HORSE_BABY, MediumFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_SADDLE, MediumSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_SADDLE_BABY, MediumSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_CARPET, MediumCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_CARPET_BABY, MediumCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_ARMOR, MediumArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_ARMOR_BABY, MediumArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_CHEST, MediumChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                MEDIUM_CHEST_BABY, MediumChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                PERCHERON_HORSE, PercheronHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_SADDLE, PercheronSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_SADDLE_BABY, PercheronSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_CARPET, PercheronCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_CARPET_BABY, PercheronCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_ARMOR, PercheronArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_ARMOR_BABY, PercheronArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_CHEST, PercheronChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                PERCHERON_CHEST_BABY, PercheronChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                SHIRE_HORSE, ShireHorseGeometry::createBodyLayer);

        event.registerLayerDefinition(
                SHIRE_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_SADDLE, ShireSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_SADDLE_BABY, ShireSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_CARPET, ShireCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_CARPET_BABY, ShireCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_ARMOR, ShireArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_ARMOR_BABY, ShireArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_CHEST, ShireChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                SHIRE_CHEST_BABY, ShireChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                BELGIAN_HORSE, BelgianHorseGeometry::createBodyLayer);

        event.registerLayerDefinition(
                BELGIAN_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_SADDLE, BelgianSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_SADDLE_BABY, BelgianSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_CARPET, BelgianCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_CARPET_BABY, BelgianCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_ARMOR, BelgianArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_ARMOR_BABY, BelgianArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_CHEST, BelgianChestGeometry::createBodyLayer);
        event.registerLayerDefinition(
                BELGIAN_CHEST_BABY, BelgianChestGeometry::createBodyLayer);

        event.registerLayerDefinition(
                CLYDESDALE_HORSE, ClydesdaleHorseGeometry::createBodyLayer);

        event.registerLayerDefinition(
                CLYDESDALE_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);

        event.registerLayerDefinition(ANDALUSIAN_DONKEY, AndalusianDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_BABY, SmallDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY, AsinaraDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_BABY, SmallDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY, MammothDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_BABY, LargeDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY, MiniatureDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_BABY, MiniatureDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY, PegaDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_BABY, PegaDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY, PoitouDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_BABY, LargeDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY, ZamoranoDonkeyGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_BABY, LargeDonkeyFoalGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_SADDLE, AndalusianDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_SADDLE_BABY, AndalusianDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_CARPET, AndalusianDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_CARPET_BABY, AndalusianDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_ARMOR, AndalusianDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_ARMOR_BABY, AndalusianDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_CHEST, AndalusianDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(ANDALUSIAN_DONKEY_CHEST_BABY, AndalusianDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_SADDLE, AsinaraDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_SADDLE_BABY, AsinaraDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_CARPET, AsinaraDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_CARPET_BABY, AsinaraDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_ARMOR, AsinaraDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_ARMOR_BABY, AsinaraDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_CHEST, AsinaraDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(ASINARA_DONKEY_CHEST_BABY, AsinaraDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_SADDLE, MammothDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_SADDLE_BABY, MammothDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_CARPET, MammothDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_CARPET_BABY, MammothDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_ARMOR, MammothDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_ARMOR_BABY, MammothDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_CHEST, MammothDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MAMMOTH_DONKEY_CHEST_BABY, MammothDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_SADDLE, MiniatureDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_SADDLE_BABY, MiniatureDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_CARPET, MiniatureDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_CARPET_BABY, MiniatureDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_ARMOR, MiniatureDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_ARMOR_BABY, MiniatureDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_CHEST, MiniatureDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MINIATURE_DONKEY_CHEST_BABY, MiniatureDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_SADDLE, PegaDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_SADDLE_BABY, PegaDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_CARPET, PegaDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_CARPET_BABY, PegaDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_ARMOR, PegaDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_ARMOR_BABY, PegaDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_CHEST, PegaDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(PEGA_DONKEY_CHEST_BABY, PegaDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_SADDLE, PoitouDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_SADDLE_BABY, PoitouDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_CARPET, PoitouDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_CARPET_BABY, PoitouDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_ARMOR, PoitouDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_ARMOR_BABY, PoitouDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_CHEST, PoitouDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(POITOU_DONKEY_CHEST_BABY, PoitouDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_SADDLE, ZamoranoDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_SADDLE_BABY, ZamoranoDonkeySaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_CARPET, ZamoranoDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_CARPET_BABY, ZamoranoDonkeyCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_ARMOR, ZamoranoDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_ARMOR_BABY, ZamoranoDonkeyArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_CHEST, ZamoranoDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(ZAMORANO_DONKEY_CHEST_BABY, ZamoranoDonkeyChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE, MuleGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_SADDLE, MuleSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_SADDLE_BABY, MuleSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_CARPET, MuleCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_CARPET_BABY, MuleCarpetGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_ARMOR, MuleArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_ARMOR_BABY, MuleArmorGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_CHEST, MuleChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_CHEST_BABY, MuleChestGeometry::createBodyLayer);
        event.registerLayerDefinition(MULE_BABY, SmallDonkeyFoalGeometry::createBodyLayer);

        registerPlates(event, ICELANDIC_ARMOR, IcelandicArmorGeometry::createBodyLayer);
        registerPlates(event, FRIESIAN_ARMOR, FriesianArmorGeometry::createBodyLayer);
        registerPlates(event, SMALL_ARMOR, SmallArmorGeometry::createBodyLayer);
        registerPlates(event, HAFLINGER_ARMOR, HaflingerArmorGeometry::createBodyLayer);
        registerPlates(event, MEDIUM_ARMOR, MediumArmorGeometry::createBodyLayer);
        registerPlates(event, PERCHERON_ARMOR, PercheronArmorGeometry::createBodyLayer);
        registerPlates(event, SHIRE_ARMOR, ShireArmorGeometry::createBodyLayer);
        registerPlates(event, BELGIAN_ARMOR, BelgianArmorGeometry::createBodyLayer);
        registerPlates(event, ANDALUSIAN_DONKEY_ARMOR, AndalusianDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, ASINARA_DONKEY_ARMOR, AsinaraDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, MAMMOTH_DONKEY_ARMOR, MammothDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, MINIATURE_DONKEY_ARMOR, MiniatureDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, PEGA_DONKEY_ARMOR, PegaDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, POITOU_DONKEY_ARMOR, PoitouDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, ZAMORANO_DONKEY_ARMOR, ZamoranoDonkeyArmorGeometry::createBodyLayer);
        registerPlates(event, MULE_ARMOR, MuleArmorGeometry::createBodyLayer);
    }

    private static void registerPlates(EntityRenderersEvent.RegisterLayerDefinitions event,
                                       ModelLayerLocation armor,
                                       Function<BhNeckPlates, LayerDefinition> geometry) {
        for (BhNeckPlates plates : BhNeckPlates.values()) {
            event.registerLayerDefinition(plates.layer(armor), () -> geometry.apply(plates));
        }
    }

    private static ModelLayerLocation layer(String path, String name) {
        return new ModelLayerLocation(
                ResourceLocation.fromNamespaceAndPath("icys-better-horses", path), name);
    }
}
