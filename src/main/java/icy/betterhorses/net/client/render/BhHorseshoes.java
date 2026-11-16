package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.function.Supplier;

public enum BhHorseshoes {
    ICELANDIC("icelandic", BhModelLayers.ICELANDIC_HORSE, IcelandicHorseshoeGeometry::createBodyLayer),
    FRIESIAN("friesian", BhModelLayers.FRIESIAN_HORSE, FriesianHorseshoeGeometry::createBodyLayer),
    SMALL("small", BhModelLayers.SMALL_HORSE, SmallHorseshoeGeometry::createBodyLayer),
    HAFLINGER("haflinger", BhModelLayers.HAFLINGER_HORSE, HaflingerHorseshoeGeometry::createBodyLayer),
    MEDIUM("medium", BhModelLayers.MEDIUM_HORSE, MediumHorseshoeGeometry::createBodyLayer),
    PERCHERON("percheron", BhModelLayers.PERCHERON_HORSE, PercheronHorseshoeGeometry::createBodyLayer),
    SHIRE("shire", BhModelLayers.SHIRE_HORSE, ShireHorseshoeGeometry::createBodyLayer),
    BELGIAN("belgian", BhModelLayers.BELGIAN_HORSE, BelgianHorseshoeGeometry::createBodyLayer),
    CLYDESDALE("clydesdale", BhModelLayers.CLYDESDALE_HORSE, ClydesdaleHorseshoeGeometry::createBodyLayer),
    SMALL_UNDEAD("small_undead", BhModelLayers.SMALL_UNDEAD_HORSE, SmallUndeadHorseshoeGeometry::createBodyLayer),
    MEDIUM_UNDEAD("medium_undead", BhModelLayers.MEDIUM_UNDEAD_HORSE, MediumUndeadHorseshoeGeometry::createBodyLayer),
    LARGE_UNDEAD("large_undead", BhModelLayers.LARGE_UNDEAD_HORSE, LargeUndeadHorseshoeGeometry::createBodyLayer),
    ANDALUSIAN_DONKEY("andalusian_donkey", BhModelLayers.ANDALUSIAN_DONKEY, AndalusianDonkeyHorseshoeGeometry::createBodyLayer),
    ASINARA_DONKEY("asinara_donkey", BhModelLayers.ASINARA_DONKEY, AsinaraDonkeyHorseshoeGeometry::createBodyLayer),
    MAMMOTH_DONKEY("mammoth_donkey", BhModelLayers.MAMMOTH_DONKEY, MammothDonkeyHorseshoeGeometry::createBodyLayer),
    MINIATURE_DONKEY("miniature_donkey", BhModelLayers.MINIATURE_DONKEY, MiniatureDonkeyHorseshoeGeometry::createBodyLayer),
    PEGA_DONKEY("pega_donkey", BhModelLayers.PEGA_DONKEY, PegaDonkeyHorseshoeGeometry::createBodyLayer),
    POITOU_DONKEY("poitou_donkey", BhModelLayers.POITOU_DONKEY, PoitouDonkeyHorseshoeGeometry::createBodyLayer),
    ZAMORANO_DONKEY("zamorano_donkey", BhModelLayers.ZAMORANO_DONKEY, ZamoranoDonkeyHorseshoeGeometry::createBodyLayer),
    SMALL_MULE("small_mule", BhModelLayers.SMALL_MULE, SmallMuleHorseshoeGeometry::createBodyLayer),
    MULE("mule", BhModelLayers.MULE, MuleHorseshoeGeometry::createBodyLayer),
    LARGE_MULE("large_mule", BhModelLayers.LARGE_MULE, LargeMuleHorseshoeGeometry::createBodyLayer);

    private final ModelLayerLocation body;
    private final ModelLayerLocation layer;
    private final ResourceLocation texture;
    private final Supplier<LayerDefinition> geometry;

    BhHorseshoes(String folder, ModelLayerLocation body, Supplier<LayerDefinition> geometry) {
        this.body = body;
        this.layer = new ModelLayerLocation(body.getModel(), "horseshoe");
        this.texture = new ResourceLocation("icys-better-horses", "textures/entity/horse/" + folder + "/horseshoe.png");
        this.geometry = geometry;
    }

    public ModelLayerLocation layer() {
        return layer;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public static BhHorseshoes of(ModelLayerLocation body) {
        for (BhHorseshoes s : values()) {
            if (s.body.equals(body)) return s;
        }
        throw new IllegalArgumentException("no horseshoes for " + body);
    }

    static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (BhHorseshoes s : values()) {
            event.registerLayerDefinition(s.layer, s.geometry);
        }
    }
}
