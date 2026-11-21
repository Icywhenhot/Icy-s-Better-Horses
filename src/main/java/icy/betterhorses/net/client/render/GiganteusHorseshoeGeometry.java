package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class GiganteusHorseshoeGeometry {

    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private GiganteusHorseshoeGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -6.0F, 1.0F));

        root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, 0.1100F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(-4.0F, 23.0F, -10.0F));

        root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, 0.1100F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(4.0F, 23.0F, -10.0F));

        root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, 0.1100F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(-4.0F, 23.0F, 14.0F));

        root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, 0.1100F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.1450F)),
                PartPose.offset(4.0F, 23.0F, 14.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
