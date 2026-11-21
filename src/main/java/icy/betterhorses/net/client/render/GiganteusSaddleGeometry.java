package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class GiganteusSaddleGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private GiganteusSaddleGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -6.0F, 1.0F));

        PartDefinition p_saddle2 = p_body.addOrReplaceChild(
                "saddle2",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -7.0F, -9.0F, 14.0F, 14.0F, 24.0F, new CubeDeformation(0.3000F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_left_saddle = p_saddle2.addOrReplaceChild(
                "left_saddle",
                CubeListBuilder.create(),
                PartPose.offset(7.0F, 0.0F, -1.0F));

        PartDefinition p_right_saddle = p_saddle2.addOrReplaceChild(
                "right_saddle",
                CubeListBuilder.create(),
                PartPose.offset(-7.0F, 0.0F, -1.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, -13.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(38, 38).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 9.0F, 9.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, -9.0F, -2.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(72, 38).addBox(-2.0F, -1.0F, -6.0F, 4.0F, 9.0F, 7.0F, new CubeDeformation(0.1000F))
                        .texOffs(94, 38).addBox(1.5000F, 1.0F, -5.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(100, 38).addBox(-2.5000F, 1.0F, -5.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -8.0F, -5.0F));

        PartDefinition p_left_rein2 = p_snout2.addOrReplaceChild(
                "left_rein2",
                CubeListBuilder.create(),
                PartPose.offset(2.5000F, 0.5000F, -3.5000F));

        PartDefinition p_left_rein3 = p_left_rein2.addOrReplaceChild(
                "left_rein3",
                CubeListBuilder.create(),
                PartPose.offset(-0.0F, -0.0F, 0.0F));

        PartDefinition p_left_saddle_line_rot = p_left_rein3.addOrReplaceChild(
                "left_saddle_line_rot",
                CubeListBuilder.create()
                        .texOffs(76, 0).addBox(0.5000F, -2.0F, -0.5938F, 0.0F, 4.0F, 19.0F),
                PartPose.offsetAndRotation(-0.5000F, 2.0F, 0.5938F, -0.0F, 0.2182F, 0.0F));

        PartDefinition p_right_rein2 = p_snout2.addOrReplaceChild(
                "right_rein2",
                CubeListBuilder.create(),
                PartPose.offset(-2.5000F, 0.5000F, -3.5000F));

        PartDefinition p_right_rein3 = p_right_rein2.addOrReplaceChild(
                "right_rein3",
                CubeListBuilder.create(),
                PartPose.offset(-0.0F, -0.0F, 0.0F));

        PartDefinition p_right_saddle_line_rot = p_right_rein3.addOrReplaceChild(
                "right_saddle_line_rot",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-0.5000F, -2.0F, -0.5938F, 0.0F, 4.0F, 19.0F),
                PartPose.offsetAndRotation(0.5000F, 2.0F, 0.5938F, -0.0F, -0.2182F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
