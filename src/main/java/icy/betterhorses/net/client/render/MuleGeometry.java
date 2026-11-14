package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometry for the Mule horse.
 *
 * <p>GENERATED from
 * {@code blockbench/mules/medium/mule.bbmodel}
 * via the Fresh-Animations rig. Do not hand-edit: re-run the generator instead, which
 * verifies every cube round-trips back to the Blockbench source.
 *
 * <p>The part names deliberately mirror the CEM rig rather than vanilla's, because
 * the animator drives a finer skeleton than vanilla exposes: vanilla lumps head,
 * ears, mouth, neck and mane into one swivelling container, which cannot express an
 * arching neck or a muzzle that chews independently.
 */
public final class MuleGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private MuleGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(3, 2).addBox(-4.0F, -5.0F, -11.0F, 8.0F, 10.0F, 20.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, -9.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(0, 62).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 14.0F, 7.0F, new CubeDeformation(0.0100F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(65, 19).addBox(-2.5000F, -6.0F, -4.0F, 5.0F, 6.0F, 7.0F)
                        .texOffs(108, 85).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offset(0.0F, -8.0F, -2.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(23, 65).addBox(-1.5000F, -1.0F, -6.0F, 3.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -4.0F, -4.0F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create()
                        .texOffs(6, 14).addBox(-0.4500F, -5.0F, -1.0F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(1.0F, -6.0F, 2.9900F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create()
                        .texOffs(0, 8).addBox(-1.5500F, -5.0F, -1.0F, 2.0F, 5.0F, 1.0F),
                PartPose.offset(-1.0F, -6.0F, 2.9900F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create()
                        .texOffs(102, 103).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 4.0F, 3.0F)
                        .texOffs(56, 35).addBox(-0.0F, -4.0F, 3.0F, 0.0F, 4.0F, 3.0F),
                PartPose.offset(0.0F, -3.0F, 3.0F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create()
                        .texOffs(80, 104).addBox(-1.0F, -11.0F, 0.0F, 2.0F, 11.0F, 3.0F)
                        .texOffs(56, 44).addBox(-0.0F, -11.0F, 3.0F, 0.0F, 11.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create()
                        .texOffs(74, 62).addBox(-1.5000F, -0.0F, 0.0F, 3.0F, 12.0F, 4.0F)
                        .texOffs(80, 32).addBox(-1.5000F, 12.0F, 0.0F, 3.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, -5.0F, 9.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(43, 65).addBox(-1.5000F, -10.0F, -1.5000F, 3.0F, 11.0F, 3.0F)
                        .texOffs(23, 77).addBox(-1.5000F, -3.0F, -1.5000F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(-2.5000F, 23.0F, -9.4000F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(66, 33).addBox(-1.5000F, -10.0F, -1.5000F, 3.0F, 11.0F, 3.0F)
                        .texOffs(56, 80).addBox(-1.5000F, -3.0F, -1.5000F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(2.5000F, 23.0F, -9.4000F));

        PartDefinition p_back_right_leg = root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(59, 65).addBox(-1.5000F, -10.0F, -2.5000F, 3.0F, 11.0F, 3.0F)
                        .texOffs(39, 80).addBox(-1.5000F, -3.0F, -2.5000F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(-2.5000F, 23.0F, 8.5000F));

        PartDefinition p_back_left_leg = root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(66, 48).addBox(-1.5000F, -10.0F, -2.5000F, 3.0F, 11.0F, 3.0F)
                        .texOffs(76, 79).addBox(-1.5000F, -3.0F, -2.5000F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(2.5000F, 23.0F, 8.5000F));

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
