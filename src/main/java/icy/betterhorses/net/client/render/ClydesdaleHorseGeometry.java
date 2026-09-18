package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ClydesdaleHorseGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private ClydesdaleHorseGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -12.0F, 12.0F, 12.0F, 24.0F, new CubeDeformation(0.0100F)),
                PartPose.offset(0.0F, 2.0F, 1.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -4.0F, -11.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(0, 36).addBox(-2.5000F, -12.0F, -4.0F, 5.0F, 16.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(26, 36).addBox(-3.0F, -7.0F, -4.0F, 6.0F, 7.0F, 8.0F)
                        .texOffs(98, 96).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, -8.0F, -2.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(90, 62).addBox(-2.0F, -1.0F, -6.0F, 4.0F, 7.0F, 6.0F),
                PartPose.offset(0.0F, -6.0F, -4.0F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create()
                        .texOffs(20, 60).addBox(-0.4500F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(1.0F, -7.0F, 3.9900F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create()
                        .texOffs(20, 64).addBox(-1.5500F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(-1.0F, -7.0F, 3.9900F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create()
                        .texOffs(100, 84).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 4.0F, 3.0F)
                        .texOffs(100, 75).addBox(-0.0F, -4.0F, 3.0F, 0.0F, 4.0F, 5.0F),
                PartPose.offset(0.0F, -4.0F, 4.0F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create()
                        .texOffs(54, 36).addBox(-1.0F, -11.0F, 0.0F, 2.0F, 11.0F, 3.0F)
                        .texOffs(88, 96).addBox(-0.0F, -11.0F, 3.0F, 0.0F, 11.0F, 5.0F),
                PartPose.offset(0.0F, -1.0F, 4.0F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create()
                        .texOffs(110, 109).addBox(-2.0F, -0.0F, 0.0F, 4.0F, 14.0F, 5.0F)
                        .texOffs(53, 117).addBox(-2.0F, 14.0F, 0.0F, 4.0F, 6.0F, 5.0F),
                PartPose.offset(0.0F, -6.0F, 12.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(26, 51).addBox(-2.5000F, -15.0F, -2.5000F, 5.0F, 16.0F, 5.0F)
                        .texOffs(0, 60).addBox(-2.5000F, -17.0F, -2.5000F, 5.0F, 18.0F, 5.0F, new CubeDeformation(0.1000F))
                        .texOffs(27, 92).addBox(-2.5000F, -10.0F, -2.5000F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.1350F))
                        .texOffs(88, 115).addBox(-2.5000F, -9.0F, 2.4000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F))
                        .texOffs(88, 115).addBox(2.5000F, -9.0F, 2.4000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F)),
                PartPose.offset(-3.5000F, 23.0F, -8.4000F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(46, 51).addBox(-2.5000F, -15.0F, -2.5000F, 5.0F, 16.0F, 5.0F)
                        .texOffs(66, 36).addBox(-2.5000F, -17.0F, -2.5000F, 5.0F, 18.0F, 5.0F, new CubeDeformation(0.1000F))
                        .texOffs(27, 92).addBox(-2.5000F, -10.0F, -2.5000F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.1350F))
                        .texOffs(88, 115).addBox(2.5000F, -9.0F, 2.6000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F))
                        .texOffs(88, 115).addBox(-2.5000F, -9.0F, 2.4000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F)),
                PartPose.offset(3.5000F, 23.0F, -8.4000F));

        PartDefinition p_back_right_leg = root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(72, 0).addBox(-2.5000F, -22.0F, -4.5000F, 5.0F, 10.0F, 7.0F, new CubeDeformation(0.0500F))
                        .texOffs(40, 74).addBox(-2.5000F, -12.0F, -2.5000F, 5.0F, 13.0F, 5.0F)
                        .texOffs(60, 76).addBox(-2.5000F, -14.0F, -2.5000F, 5.0F, 15.0F, 5.0F, new CubeDeformation(0.1000F))
                        .texOffs(27, 92).addBox(-2.5000F, -10.0F, -2.5000F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.1350F))
                        .texOffs(88, 115).addBox(2.5000F, -9.0F, 2.7000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F))
                        .texOffs(88, 115).addBox(-2.5000F, -9.0F, 2.7000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F)),
                PartPose.offset(-3.5000F, 23.0F, 11.5000F));

        PartDefinition p_back_left_leg = root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(66, 59).addBox(-2.5000F, -22.0F, -4.5000F, 5.0F, 10.0F, 7.0F, new CubeDeformation(0.0500F))
                        .texOffs(20, 74).addBox(-2.5000F, -12.0F, -2.5000F, 5.0F, 13.0F, 5.0F)
                        .texOffs(80, 76).addBox(-2.5000F, -14.0F, -2.5000F, 5.0F, 15.0F, 5.0F, new CubeDeformation(0.1000F))
                        .texOffs(27, 92).addBox(-2.5000F, -10.0F, -2.5000F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.1350F))
                        .texOffs(88, 115).addBox(2.5000F, -9.0F, 2.7000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F))
                        .texOffs(88, 115).addBox(-2.5000F, -9.0F, 2.7000F, 0.0F, 10.0F, 3.0F, new CubeDeformation(0.0900F)),
                PartPose.offset(3.5000F, 23.0F, 11.5000F));

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
