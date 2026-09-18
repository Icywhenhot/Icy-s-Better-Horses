package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

public final class BhModelLayers {

    public static final ModelLayerLocation ICELANDIC_HORSE = layer("icelandic_horse", "main");
    public static final ModelLayerLocation ICELANDIC_HORSE_BABY = layer("icelandic_horse", "baby");
    public static final ModelLayerLocation ICELANDIC_SADDLE = layer("icelandic_horse", "saddle");
    public static final ModelLayerLocation ICELANDIC_SADDLE_BABY = layer("icelandic_horse", "saddle_baby");
    public static final ModelLayerLocation ICELANDIC_ARMOR = layer("icelandic_horse", "armor");
    public static final ModelLayerLocation ICELANDIC_ARMOR_BABY = layer("icelandic_horse", "armor_baby");
    public static final ModelLayerLocation ICELANDIC_CHEST = layer("icelandic_horse", "chest");
    public static final ModelLayerLocation ICELANDIC_CHEST_BABY = layer("icelandic_horse", "chest_baby");

    public static final ModelLayerLocation FRIESIAN_HORSE = layer("friesian_horse", "main");
    public static final ModelLayerLocation FRIESIAN_HORSE_BABY = layer("friesian_horse", "baby");
    public static final ModelLayerLocation FRIESIAN_SADDLE = layer("friesian_horse", "saddle");
    public static final ModelLayerLocation FRIESIAN_SADDLE_BABY = layer("friesian_horse", "saddle_baby");
    public static final ModelLayerLocation FRIESIAN_ARMOR = layer("friesian_horse", "armor");
    public static final ModelLayerLocation FRIESIAN_ARMOR_BABY = layer("friesian_horse", "armor_baby");
    public static final ModelLayerLocation FRIESIAN_CHEST = layer("friesian_horse", "chest");
    public static final ModelLayerLocation FRIESIAN_CHEST_BABY = layer("friesian_horse", "chest_baby");

    public static final ModelLayerLocation SMALL_HORSE = layer("small_horse", "main");
    public static final ModelLayerLocation SMALL_HORSE_BABY = layer("small_horse", "baby");
    public static final ModelLayerLocation SMALL_SADDLE = layer("small_horse", "saddle");
    public static final ModelLayerLocation SMALL_SADDLE_BABY = layer("small_horse", "saddle_baby");
    public static final ModelLayerLocation SMALL_ARMOR = layer("small_horse", "armor");
    public static final ModelLayerLocation SMALL_ARMOR_BABY = layer("small_horse", "armor_baby");
    public static final ModelLayerLocation SMALL_CHEST = layer("small_horse", "chest");
    public static final ModelLayerLocation SMALL_CHEST_BABY = layer("small_horse", "chest_baby");

    public static final ModelLayerLocation HAFLINGER_HORSE = layer("haflinger_horse", "main");
    public static final ModelLayerLocation HAFLINGER_HORSE_BABY = layer("haflinger_horse", "baby");
    public static final ModelLayerLocation HAFLINGER_SADDLE = layer("haflinger_horse", "saddle");
    public static final ModelLayerLocation HAFLINGER_SADDLE_BABY = layer("haflinger_horse", "saddle_baby");
    public static final ModelLayerLocation HAFLINGER_ARMOR = layer("haflinger_horse", "armor");
    public static final ModelLayerLocation HAFLINGER_ARMOR_BABY = layer("haflinger_horse", "armor_baby");
    public static final ModelLayerLocation HAFLINGER_CHEST = layer("haflinger_horse", "chest");
    public static final ModelLayerLocation HAFLINGER_CHEST_BABY = layer("haflinger_horse", "chest_baby");

    public static final ModelLayerLocation MEDIUM_HORSE = layer("medium_horse", "main");
    public static final ModelLayerLocation MEDIUM_HORSE_BABY = layer("medium_horse", "baby");
    public static final ModelLayerLocation MEDIUM_SADDLE = layer("medium_horse", "saddle");
    public static final ModelLayerLocation MEDIUM_SADDLE_BABY = layer("medium_horse", "saddle_baby");
    public static final ModelLayerLocation MEDIUM_ARMOR = layer("medium_horse", "armor");
    public static final ModelLayerLocation MEDIUM_ARMOR_BABY = layer("medium_horse", "armor_baby");
    public static final ModelLayerLocation MEDIUM_CHEST = layer("medium_horse", "chest");
    public static final ModelLayerLocation MEDIUM_CHEST_BABY = layer("medium_horse", "chest_baby");

    public static final ModelLayerLocation PERCHERON_HORSE = layer("percheron_horse", "main");
    public static final ModelLayerLocation PERCHERON_HORSE_BABY = layer("percheron_horse", "baby");
    public static final ModelLayerLocation PERCHERON_SADDLE = layer("percheron_horse", "saddle");
    public static final ModelLayerLocation PERCHERON_SADDLE_BABY = layer("percheron_horse", "saddle_baby");
    public static final ModelLayerLocation PERCHERON_ARMOR = layer("percheron_horse", "armor");
    public static final ModelLayerLocation PERCHERON_ARMOR_BABY = layer("percheron_horse", "armor_baby");
    public static final ModelLayerLocation PERCHERON_CHEST = layer("percheron_horse", "chest");
    public static final ModelLayerLocation PERCHERON_CHEST_BABY = layer("percheron_horse", "chest_baby");

    public static final ModelLayerLocation SHIRE_HORSE = layer("shire_horse", "main");
    public static final ModelLayerLocation SHIRE_HORSE_BABY = layer("shire_horse", "baby");
    public static final ModelLayerLocation SHIRE_SADDLE = layer("shire_horse", "saddle");
    public static final ModelLayerLocation SHIRE_SADDLE_BABY = layer("shire_horse", "saddle_baby");
    public static final ModelLayerLocation SHIRE_ARMOR = layer("shire_horse", "armor");
    public static final ModelLayerLocation SHIRE_ARMOR_BABY = layer("shire_horse", "armor_baby");
    public static final ModelLayerLocation SHIRE_CHEST = layer("shire_horse", "chest");
    public static final ModelLayerLocation SHIRE_CHEST_BABY = layer("shire_horse", "chest_baby");

    public static final ModelLayerLocation BELGIAN_HORSE = layer("belgian_horse", "main");
    public static final ModelLayerLocation BELGIAN_HORSE_BABY = layer("belgian_horse", "baby");
    public static final ModelLayerLocation BELGIAN_SADDLE = layer("belgian_horse", "saddle");
    public static final ModelLayerLocation BELGIAN_SADDLE_BABY = layer("belgian_horse", "saddle_baby");
    public static final ModelLayerLocation BELGIAN_ARMOR = layer("belgian_horse", "armor");
    public static final ModelLayerLocation BELGIAN_ARMOR_BABY = layer("belgian_horse", "armor_baby");
    public static final ModelLayerLocation BELGIAN_CHEST = layer("belgian_horse", "chest");
    public static final ModelLayerLocation BELGIAN_CHEST_BABY = layer("belgian_horse", "chest_baby");

    public static final ModelLayerLocation CLYDESDALE_HORSE = layer("clydesdale_horse", "main");
    public static final ModelLayerLocation CLYDESDALE_HORSE_BABY = layer("clydesdale_horse", "baby");

    private BhModelLayers() {}

    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                ICELANDIC_HORSE, IcelandicHorseGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_HORSE_BABY, IcelandicFoalGeometry::createBodyLayer);

        event.registerLayerDefinition(
                ICELANDIC_SADDLE, IcelandicSaddleGeometry::createBodyLayer);
        event.registerLayerDefinition(
                ICELANDIC_SADDLE_BABY, IcelandicSaddleGeometry::createBodyLayer);
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
    }

    private static ModelLayerLocation layer(String path, String name) {
        return new ModelLayerLocation(
                new ResourceLocation("icys-better-horses", path), name);
    }
}
