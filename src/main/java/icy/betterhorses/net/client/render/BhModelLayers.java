package icy.betterhorses.net.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

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

    public static void register() {
        ModelLayerRegistry.registerModelLayer(LARGE_UNDEAD_HORSE, LargeUndeadHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MEDIUM_UNDEAD_HORSE, MediumUndeadHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SMALL_UNDEAD_HORSE, SmallUndeadHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_HORSE, IcelandicHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_HORSE_BABY, IcelandicFoalGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_SADDLE, IcelandicSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_SADDLE_BABY, IcelandicSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_CARPET, IcelandicCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_CARPET_BABY, IcelandicCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_ARMOR, IcelandicArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_ARMOR_BABY, IcelandicArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_CHEST, IcelandicChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_CHEST_BABY, IcelandicChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_HORSE, FriesianHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_HORSE_BABY, FriesianFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_SADDLE, FriesianSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_SADDLE_BABY, FriesianSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_CARPET, FriesianCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_CARPET_BABY, FriesianCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_ARMOR, FriesianArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_ARMOR_BABY, FriesianArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_CHEST, FriesianChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_CHEST_BABY, FriesianChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                SMALL_HORSE, SmallHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_HORSE_BABY, SmallFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_SADDLE, SmallSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_SADDLE_BABY, SmallSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_CARPET, SmallCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_CARPET_BABY, SmallCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_ARMOR, SmallArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_ARMOR_BABY, SmallArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_CHEST, SmallChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SMALL_CHEST_BABY, SmallChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_HORSE, HaflingerHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_HORSE_BABY, SmallFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_SADDLE, HaflingerSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_SADDLE_BABY, HaflingerSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_CARPET, HaflingerCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_CARPET_BABY, HaflingerCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_ARMOR, HaflingerArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_ARMOR_BABY, HaflingerArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_CHEST, HaflingerChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                HAFLINGER_CHEST_BABY, HaflingerChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                MEDIUM_HORSE, MediumHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_HORSE_BABY, MediumFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_SADDLE, MediumSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_SADDLE_BABY, MediumSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_CARPET, MediumCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_CARPET_BABY, MediumCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_ARMOR, MediumArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_ARMOR_BABY, MediumArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_CHEST, MediumChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_CHEST_BABY, MediumChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                PERCHERON_HORSE, PercheronHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_SADDLE, PercheronSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_SADDLE_BABY, PercheronSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_CARPET, PercheronCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_CARPET_BABY, PercheronCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_ARMOR, PercheronArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_ARMOR_BABY, PercheronArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_CHEST, PercheronChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                PERCHERON_CHEST_BABY, PercheronChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                SHIRE_HORSE, ShireHorseGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                SHIRE_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_SADDLE, ShireSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_SADDLE_BABY, ShireSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_CARPET, ShireCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_CARPET_BABY, ShireCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_ARMOR, ShireArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_ARMOR_BABY, ShireArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_CHEST, ShireChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                SHIRE_CHEST_BABY, ShireChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                BELGIAN_HORSE, BelgianHorseGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                BELGIAN_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_SADDLE, BelgianSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_SADDLE_BABY, BelgianSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_CARPET, BelgianCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_CARPET_BABY, BelgianCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_ARMOR, BelgianArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_ARMOR_BABY, BelgianArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_CHEST, BelgianChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                BELGIAN_CHEST_BABY, BelgianChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                CLYDESDALE_HORSE, ClydesdaleHorseGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                CLYDESDALE_HORSE_BABY, PercheronFoalGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY, AndalusianDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_BABY, SmallDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY, AsinaraDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_BABY, SmallDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY, MammothDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_BABY, LargeDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY, MiniatureDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_BABY, MiniatureDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY, PegaDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_BABY, PegaDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY, PoitouDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_BABY, LargeDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY, ZamoranoDonkeyGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_BABY, LargeDonkeyFoalGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_SADDLE, AndalusianDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_SADDLE_BABY, AndalusianDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_CARPET, AndalusianDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_CARPET_BABY, AndalusianDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_ARMOR, AndalusianDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_ARMOR_BABY, AndalusianDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_CHEST, AndalusianDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ANDALUSIAN_DONKEY_CHEST_BABY, AndalusianDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_SADDLE, AsinaraDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_SADDLE_BABY, AsinaraDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_CARPET, AsinaraDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_CARPET_BABY, AsinaraDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_ARMOR, AsinaraDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_ARMOR_BABY, AsinaraDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_CHEST, AsinaraDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ASINARA_DONKEY_CHEST_BABY, AsinaraDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_SADDLE, MammothDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_SADDLE_BABY, MammothDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_CARPET, MammothDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_CARPET_BABY, MammothDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_ARMOR, MammothDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_ARMOR_BABY, MammothDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_CHEST, MammothDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MAMMOTH_DONKEY_CHEST_BABY, MammothDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_SADDLE, MiniatureDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_SADDLE_BABY, MiniatureDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_CARPET, MiniatureDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_CARPET_BABY, MiniatureDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_ARMOR, MiniatureDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_ARMOR_BABY, MiniatureDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_CHEST, MiniatureDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(MINIATURE_DONKEY_CHEST_BABY, MiniatureDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_SADDLE, PegaDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_SADDLE_BABY, PegaDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_CARPET, PegaDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_CARPET_BABY, PegaDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_ARMOR, PegaDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_ARMOR_BABY, PegaDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_CHEST, PegaDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PEGA_DONKEY_CHEST_BABY, PegaDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_SADDLE, PoitouDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_SADDLE_BABY, PoitouDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_CARPET, PoitouDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_CARPET_BABY, PoitouDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_ARMOR, PoitouDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_ARMOR_BABY, PoitouDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_CHEST, PoitouDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(POITOU_DONKEY_CHEST_BABY, PoitouDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_SADDLE, ZamoranoDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_SADDLE_BABY, ZamoranoDonkeySaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_CARPET, ZamoranoDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_CARPET_BABY, ZamoranoDonkeyCarpetGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_ARMOR, ZamoranoDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_ARMOR_BABY, ZamoranoDonkeyArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_CHEST, ZamoranoDonkeyChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(ZAMORANO_DONKEY_CHEST_BABY, ZamoranoDonkeyChestGeometry::createBodyLayer);
    }

    private static ModelLayerLocation layer(String path, String name) {
        return new ModelLayerLocation(
                Identifier.fromNamespaceAndPath("icys-better-horses", path), name);
    }
}
