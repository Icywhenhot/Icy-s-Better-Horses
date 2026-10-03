package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public enum BhNeckPlates {
    COPPER("copper", 0, 0, -1.0F, 2.0F),
    EXPOSED("exposed", 2, 0, 0.0F, 2.0F),
    OXIDIZED("oxidized", 3, 1, 0.0F, 1.0F);

    private static final float HEIGHT = 9.0F;

    private final String id;
    private final int u;
    private final int v;
    private final float behind;
    private final float depth;

    BhNeckPlates(String id, int u, int v, float behind, float depth) {
        this.id = id;
        this.u = u;
        this.v = v;
        this.behind = behind;
        this.depth = depth;
    }

    public void add(PartDefinition neck, float halfWidth, float top, float back) {
        neck.addOrReplaceChild("neck_plates",
                CubeListBuilder.create()
                        .texOffs(u, v).addBox(halfWidth, top, back + behind, 0.0F, HEIGHT, depth)
                        .texOffs(u, v).addBox(-halfWidth, top, back + behind, 0.0F, HEIGHT, depth),
                PartPose.ZERO);
    }

    public void addRod(PartDefinition head, float top, float front, int u, int v) {
        head.addOrReplaceChild("rod",
                CubeListBuilder.create()
                        .texOffs(u, v).addBox(-2.0F, top - 6.0F, front + 1.0F, 4.0F, 4.0F, 4.0F)
                        .texOffs(u, v + 8).addBox(-1.0F, top - 2.0F, front + 2.0F, 2.0F, 2.0F, 2.0F),
                PartPose.ZERO);
    }

    public ModelLayerLocation layer(ModelLayerLocation armor) {
        return new ModelLayerLocation(armor.getModel(), armor.getLayer() + "_" + id + "_plates");
    }

    public static BhNeckPlates of(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!key.getNamespace().equals("caverns_and_chasms")) return null;
        String path = key.getPath();
        if (path.startsWith("waxed_")) path = path.substring("waxed_".length());
        return switch (path) {
            case "copper_horse_armor" -> COPPER;
            case "exposed_copper_horse_armor" -> EXPOSED;
            case "weathered_copper_horse_armor", "oxidized_copper_horse_armor" -> OXIDIZED;
            default -> null;
        };
    }
}
