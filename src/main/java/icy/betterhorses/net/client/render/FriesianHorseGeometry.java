package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometry for the Friesian horse.
 *
 * <p>GENERATED from
 * {@code blockbench/horses/freisian/Friesian.bbmodel}
 * via the Fresh-Animations rig. Do not hand-edit: re-run the generator instead, which
 * verifies every cube round-trips back to the Blockbench source.
 *
 * <p>The part names deliberately mirror the CEM rig rather than vanilla's, because
 * the animator drives a finer skeleton than vanilla exposes: vanilla lumps head,
 * ears, mouth, neck and mane into one swivelling container, which cannot express an
 * arching neck or a muzzle that chews independently.
 */
public final class FriesianHorseGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private FriesianHorseGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -5.0F, -11.0F, 10.0F, 10.0F, 22.0F)
                        .texOffs(0, 32).addBox(-5.0F, -5.0F, -11.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.0500F)),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, -9.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(0, 64).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 14.0F, 7.0F, new CubeDeformation(0.0100F))
                        .texOffs(64, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 14.0F, 7.0F, new CubeDeformation(0.0500F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(64, 21).addBox(-2.5000F, -6.0F, -4.0F, 5.0F, 6.0F, 7.0F)
                        .texOffs(22, 64).addBox(-2.5000F, -6.0F, -4.0F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.0500F))
                        .texOffs(88, 26).addBox(-1.0F, -7.0F, -2.0F, 2.0F, 1.0F, 5.0F),
                PartPose.offset(0.0F, -8.0F, -2.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(80, 34).addBox(-1.5000F, -1.0F, -6.0F, 3.0F, 5.0F, 6.0F),
                PartPose.offset(0.0F, -4.0F, -4.0F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create()
                        .texOffs(20, 92).addBox(-0.4500F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(1.0F, -6.0F, 2.9900F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create()
                        .texOffs(26, 92).addBox(-1.5500F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F),
                PartPose.offset(-1.0F, -6.0F, 2.9900F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create()
                        .texOffs(50, 91).addBox(-1.0F, -4.0F, 0.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, 3.0F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create()
                        .texOffs(42, 91).addBox(-1.0F, -11.0F, 0.0F, 2.0F, 11.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create()
                        .texOffs(64, 34).addBox(-1.5000F, -0.0F, 0.0F, 3.0F, 15.0F, 5.0F)
                        .texOffs(86, 0).addBox(-1.5000F, 15.0F, 0.0F, 3.0F, 6.0F, 5.0F),
                PartPose.offset(0.0F, -5.0F, 11.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(64, 54).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 15.0F, 4.0F)
                        .mirror().texOffs(82, 79).addBox(-2.5000F, -2.0F, -2.6000F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0100F))
                        .mirror(false).texOffs(62, 88).addBox(-0.0F, -9.0F, 1.4000F, 0.0F, 10.0F, 5.0F),
                PartPose.offset(-3.0F, 23.0F, -8.9000F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(46, 64).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 15.0F, 4.0F)
                        .texOffs(82, 79).addBox(-2.5000F, -2.0F, -2.6000F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0100F))
                        .texOffs(72, 88).addBox(-0.0F, -9.0F, 1.4000F, 0.0F, 10.0F, 5.0F),
                PartPose.offset(3.0F, 23.0F, -8.9000F));

        PartDefinition p_back_right_leg = root.addOrReplaceChild(
                "back_right_leg",
                CubeListBuilder.create()
                        .texOffs(22, 77).addBox(-2.0F, -17.0F, -4.0F, 4.0F, 9.0F, 6.0F, new CubeDeformation(0.0100F))
                        .mirror().texOffs(82, 79).addBox(-2.5000F, -2.0F, -2.5000F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0100F))
                        .mirror(false).texOffs(82, 87).addBox(-0.0F, -9.0F, 1.5000F, 0.0F, 10.0F, 5.0F)
                        .texOffs(80, 58).addBox(-2.0F, -8.0F, -2.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offset(-3.0F, 23.0F, 10.0F));

        PartDefinition p_back_left_leg = root.addOrReplaceChild(
                "back_left_leg",
                CubeListBuilder.create()
                        .texOffs(62, 73).addBox(-2.0F, -17.0F, -4.0F, 4.0F, 9.0F, 6.0F, new CubeDeformation(0.0100F))
                        .texOffs(82, 79).addBox(-2.5000F, -2.0F, -2.5000F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0100F))
                        .texOffs(88, 11).addBox(-0.0F, -9.0F, 1.5000F, 0.0F, 10.0F, 5.0F)
                        .texOffs(80, 45).addBox(-2.0F, -8.0F, -2.0F, 4.0F, 9.0F, 4.0F),
                PartPose.offset(3.0F, 23.0F, 10.0F));

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
