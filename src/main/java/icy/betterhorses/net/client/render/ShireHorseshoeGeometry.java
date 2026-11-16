package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ShireHorseshoeGeometry {

    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private ShireHorseshoeGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, 1.0F));

        root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5000F, 0.1100F, -2.5000F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(-3.5000F, 23.0F, -8.4000F));

        root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5000F, 0.1100F, -2.5000F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(3.5000F, 23.0F, -8.4000F));

        root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5000F, 0.1100F, -2.5000F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(-3.5000F, 23.0F, 11.5000F));

        root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5000F, 0.1100F, -2.5000F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(3.5000F, 23.0F, 11.5000F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
