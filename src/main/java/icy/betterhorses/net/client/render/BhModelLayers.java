package icy.betterhorses.net.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

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

    public static final ModelLayerLocation MEDIUM_HORSE = layer("medium_horse", "main");
    public static final ModelLayerLocation MEDIUM_HORSE_BABY = layer("medium_horse", "baby");
    public static final ModelLayerLocation MEDIUM_SADDLE = layer("medium_horse", "saddle");
    public static final ModelLayerLocation MEDIUM_SADDLE_BABY = layer("medium_horse", "saddle_baby");
    public static final ModelLayerLocation MEDIUM_ARMOR = layer("medium_horse", "armor");
    public static final ModelLayerLocation MEDIUM_ARMOR_BABY = layer("medium_horse", "armor_baby");
    public static final ModelLayerLocation MEDIUM_CHEST = layer("medium_horse", "chest");
    public static final ModelLayerLocation MEDIUM_CHEST_BABY = layer("medium_horse", "chest_baby");

    private BhModelLayers() {}

    public static void register() {
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_HORSE, IcelandicHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_HORSE_BABY, IcelandicHorseGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_SADDLE, IcelandicSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                ICELANDIC_SADDLE_BABY, IcelandicSaddleGeometry::createBodyLayer);
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
                FRIESIAN_HORSE_BABY, FriesianHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_SADDLE, FriesianSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_SADDLE_BABY, FriesianSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_ARMOR, FriesianArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_ARMOR_BABY, FriesianArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_CHEST, FriesianChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                FRIESIAN_CHEST_BABY, FriesianChestGeometry::createBodyLayer);

        ModelLayerRegistry.registerModelLayer(
                MEDIUM_HORSE, MediumHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_HORSE_BABY, MediumHorseGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_SADDLE, MediumSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_SADDLE_BABY, MediumSaddleGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_ARMOR, MediumArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_ARMOR_BABY, MediumArmorGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_CHEST, MediumChestGeometry::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(
                MEDIUM_CHEST_BABY, MediumChestGeometry::createBodyLayer);
    }

    private static ModelLayerLocation layer(String path, String name) {
        return new ModelLayerLocation(
                Identifier.fromNamespaceAndPath("icys-better-horses", path), name);
    }
}
