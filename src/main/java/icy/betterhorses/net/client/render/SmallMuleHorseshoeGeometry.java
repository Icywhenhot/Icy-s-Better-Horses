package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class SmallMuleHorseshoeGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private SmallMuleHorseshoeGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 7.0F, 0.0F));

        root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 51).addBox(-1.5000F, 0.0F, -1.5000F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1100F)),
                PartPose.offset(-2.5000F, 23.0F, -9.4000F));

        root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 51).addBox(-1.5000F, 0.0F, -1.5000F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1100F)),
                PartPose.offset(2.5000F, 23.0F, -9.4000F));

        root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 51).addBox(-1.5000F, 0.0F, -2.5000F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1100F)),
                PartPose.offset(-2.5000F, 23.0F, 6.5000F));

        root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 51).addBox(-1.5000F, 0.0F, -2.5000F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.1100F)),
                PartPose.offset(2.5000F, 23.0F, 6.5000F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
