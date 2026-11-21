package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class GiganteusHorseGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private GiganteusHorseGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-7.0F, -7.0F, -14.0F, 14.0F, 14.0F, 29.0F),
                PartPose.offset(0.0F, -6.0F, 1.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, -13.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(86, 0).addBox(-3.0F, -14.0F, -5.0F, 6.0F, 19.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(72, 72).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 9.0F, 9.0F)
                        .texOffs(54, 93).addBox(-1.0F, -10.0F, -2.0F, 2.0F, 1.0F, 6.0F),
                PartPose.offset(0.0F, -9.0F, -2.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(106, 72).addBox(-2.0F, -1.0F, -6.0F, 4.0F, 9.0F, 7.0F),
                PartPose.offset(0.0F, -8.0F, -5.0F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create()
                        .texOffs(70, 93).addBox(-0.0F, -3.0F, -0.9900F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(1.0F, -9.0F, 3.9900F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create()
                        .texOffs(76, 93).addBox(-2.0F, -3.0F, -0.9900F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(-1.0F, -9.0F, 3.9900F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create()
                        .texOffs(42, 93).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 5.0F, 4.0F)
                        .texOffs(30, 93).addBox(-0.0F, -5.0F, 4.0F, 0.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -5.0F, 4.0F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create()
                        .texOffs(0, 93).addBox(-1.0F, -13.0F, 0.0F, 2.0F, 13.0F, 3.0F)
                        .texOffs(60, 72).addBox(-0.0F, -13.0F, 3.0F, 0.0F, 13.0F, 6.0F),
                PartPose.offset(0.0F, -1.0F, 5.0F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create()
                        .texOffs(96, 43).addBox(-2.0F, -0.0F, 0.0F, 4.0F, 17.0F, 6.0F)
                        .texOffs(10, 93).addBox(-2.0F, 15.0F, 0.0F, 4.0F, 8.0F, 6.0F),
                PartPose.offset(0.0F, -7.0F, 15.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 43).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 23.0F, 6.0F),
                PartPose.offset(-4.0F, 23.0F, -10.0F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(24, 43).addBox(-3.0F, -22.0F, -3.0F, 6.0F, 23.0F, 6.0F),
                PartPose.offset(4.0F, 23.0F, -10.0F));

        PartDefinition p_back_right_leg = root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(30, 72).addBox(-3.0F, -30.0F, -6.0F, 6.0F, 12.0F, 9.0F, new CubeDeformation(0.0100F))
                        .texOffs(72, 43).addBox(-3.0F, -18.0F, -3.0F, 6.0F, 19.0F, 6.0F),
                PartPose.offset(-4.0F, 23.0F, 14.0F));

        PartDefinition p_back_left_leg = root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 72).addBox(-3.0F, -30.0F, -6.0F, 6.0F, 12.0F, 9.0F, new CubeDeformation(0.0100F))
                        .texOffs(48, 43).addBox(-3.0F, -18.0F, -3.0F, 6.0F, 19.0F, 6.0F),
                PartPose.offset(4.0F, 23.0F, 14.0F));

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
