package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class LargeMuleCarpetGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private LargeMuleCarpetGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition p_saddle2 = p_body.addOrReplaceChild(
                "saddle2",
                CubeListBuilder.create()
                        .texOffs(53, 74).addBox(-5.0F, -6.2000F, -11.0F, 10.0F, 16.0F, 22.0F, new CubeDeformation(0.2500F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, -10.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-2.5000F, -12.2143F, -4.0F, 5.0F, 21.0F, 8.0F, new CubeDeformation(0.2500F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(26, 36).addBox(-3.0F, -7.0F, -4.0F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.2500F)),
                PartPose.offset(0.0F, -8.0F, -2.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(26, 51).addBox(-2.0F, -2.4000F, -6.0F, 4.0F, 7.0F, 6.0F, new CubeDeformation(0.2500F))
                        .texOffs(30, 85).addBox(2.1667F, 3.1000F, -4.0F, 1.0F, 3.0F, 2.0F)
                        .texOffs(118, 12).addBox(-3.1667F, 3.1000F, -4.0F, 1.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -6.0F, -4.0F));

        PartDefinition p_left_rein2 = p_snout2.addOrReplaceChild(
                "left_rein2",
                CubeListBuilder.create(),
                PartPose.offset(3.3333F, 2.6000F, -2.5000F));

        PartDefinition p_left_rein3 = p_left_rein2.addOrReplaceChild(
                "left_rein3",
                CubeListBuilder.create(),
                PartPose.offset(-0.0F, -0.0F, 0.0F));

        PartDefinition p_left_saddle_line_rot = p_left_rein3.addOrReplaceChild(
                "left_saddle_line_rot",
                CubeListBuilder.create()
                        .texOffs(17, 65).addBox(0.6666F, -2.0F, -0.5000F, 0.0F, 4.0F, 16.0F),
                PartPose.offsetAndRotation(-0.6666F, 2.0F, 0.5000F, -0.0F, 0.2182F, 0.0F));

        PartDefinition p_right_rein2 = p_snout2.addOrReplaceChild(
                "right_rein2",
                CubeListBuilder.create(),
                PartPose.offset(-3.3333F, 2.6000F, -2.5000F));

        PartDefinition p_right_rein3 = p_right_rein2.addOrReplaceChild(
                "right_rein3",
                CubeListBuilder.create(),
                PartPose.offset(-0.0F, -0.0F, 0.0F));

        PartDefinition p_right_saddle_line_rot = p_right_rein3.addOrReplaceChild(
                "right_saddle_line_rot",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.6666F, -2.0F, -0.5000F, 0.0F, 4.0F, 16.0F),
                PartPose.offsetAndRotation(0.6666F, 2.0F, 0.5000F, -0.0F, -0.2182F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
