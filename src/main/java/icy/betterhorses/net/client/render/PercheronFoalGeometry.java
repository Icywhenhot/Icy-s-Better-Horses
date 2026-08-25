package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class PercheronFoalGeometry {

    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private PercheronFoalGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 11.0F, new CubeDeformation(0.0020F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.5000F, -2.5000F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(18, 17).addBox(-1.0F, -7.0F, -2.0F, 2.0F, 8.0F, 4.0F),
                PartPose.offset(0.0F, -0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(0, 17).addBox(-2.0F, -5.0F, -2.5000F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0010F))
                        .texOffs(32, 25).addBox(-1.0F, -5.5000F, -1.5000F, 2.0F, 2.0F, 4.0F, new CubeDeformation(-0.0010F)),
                PartPose.offset(0.0F, -5.0F, -1.5000F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(30, 17).addBox(-1.0F, -1.0F, -3.0F, 2.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, -4.0F, -2.5000F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create()
                        .texOffs(28, 40).addBox(-0.2500F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(1.0F, -4.5000F, 2.5000F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create()
                        .texOffs(40, 39).addBox(-0.7500F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(-1.0F, -4.5000F, 2.5000F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create()
                        .texOffs(20, 40).addBox(-0.5000F, -4.0F, 0.0F, 1.0F, 4.0F, 1.0F)
                        .texOffs(24, 40).addBox(-0.0F, -4.0F, 1.0F, 0.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -1.5000F, 2.5000F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create()
                        .texOffs(16, 40).addBox(-0.5000F, -7.0F, 0.0F, 1.0F, 7.0F, 1.0F)
                        .texOffs(40, 17).addBox(-0.0F, -7.0F, 0.5000F, 0.0F, 6.0F, 2.0F),
                PartPose.offset(0.0F, -0.5000F, 2.0F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create()
                        .texOffs(34, 8).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 7.0F, 2.0F)
                        .texOffs(32, 39).addBox(-1.0F, 6.0F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.0010F)),
                PartPose.offset(0.0F, -1.0F, 7.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(8, 28).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offset(-2.0F, 23.0F, -3.0F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 29).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offset(2.0F, 23.0F, -3.0F));

        PartDefinition p_back_right_leg = root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(34, 0).addBox(-1.0F, -13.0F, -2.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0010F))
                        .texOffs(24, 29).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offset(-2.0F, 23.0F, 7.0F));

        PartDefinition p_back_left_leg = root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(32, 31).addBox(-1.0F, -13.0F, -2.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0010F))
                        .texOffs(16, 29).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 9.0F, 2.0F),
                PartPose.offset(2.0F, 23.0F, 7.0F));

        PartDefinition p_tail = root.addOrReplaceChild(
                "tail",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition p_saddle = p_tail.addOrReplaceChild(
                "saddle",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_right_saddle = p_tail.addOrReplaceChild(
                "right_saddle",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_left_saddle = p_tail.addOrReplaceChild(
                "left_saddle",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_headpiece_neck = p_tail.addOrReplaceChild(
                "headpiece_neck",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_headpiece_head = p_tail.addOrReplaceChild(
                "headpiece_head",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_headpiece_snout = p_tail.addOrReplaceChild(
                "headpiece_snout",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_left_rein2 = p_tail.addOrReplaceChild(
                "left_rein2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_left_rein3 = p_tail.addOrReplaceChild(
                "left_rein3",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_right_rein2 = p_tail.addOrReplaceChild(
                "right_rein2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_right_rein3 = p_tail.addOrReplaceChild(
                "right_rein3",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
