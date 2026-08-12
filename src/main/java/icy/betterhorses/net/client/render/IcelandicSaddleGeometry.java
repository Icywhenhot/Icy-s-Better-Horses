package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometry for the Icelandic horse's saddle.
 *
 * <p>GENERATED from
 * {@code blockbench/horses/icelandic/saddle.bbmodel}
 * via the Fresh-Animations rig. Do not hand-edit: re-run the generator instead, which
 * verifies every cube round-trips back to the Blockbench source.
 *
 * <p>The part names deliberately mirror the CEM rig rather than vanilla's, because
 * the animator drives a finer skeleton than vanilla exposes: vanilla lumps head,
 * ears, mouth, neck and mane into one swivelling container, which cannot express an
 * arching neck or a muzzle that chews independently.
 */
public final class IcelandicSaddleGeometry {

    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private IcelandicSaddleGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition p_saddle2 = p_body.addOrReplaceChild(
                "saddle2",
                CubeListBuilder.create()
                        .texOffs(11, 5).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 9.0F, 7.0F, new CubeDeformation(0.3000F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_left_saddle = p_saddle2.addOrReplaceChild(
                "left_saddle",
                CubeListBuilder.create(),
                PartPose.offset(4.0F, 0.0F, -1.0F));

        PartDefinition p_right_saddle = p_saddle2.addOrReplaceChild(
                "right_saddle",
                CubeListBuilder.create(),
                PartPose.offset(-4.0F, 0.0F, -1.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, -9.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-2.5000F, -5.0F, -4.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, -8.0F, -1.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(24, 46).addBox(-1.5000F, -1.0F, -5.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0600F))
                        .texOffs(40, 46).addBox(1.5000F, 1.0F, -4.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(46, 46).addBox(-2.5000F, 1.0F, -4.0F, 1.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, -4.0F));

        PartDefinition p_left_rein2 = p_snout2.addOrReplaceChild(
                "left_rein2",
                CubeListBuilder.create(),
                PartPose.offset(2.5000F, 0.5000F, -2.5000F));

        PartDefinition p_left_rein3 = p_left_rein2.addOrReplaceChild(
                "left_rein3",
                CubeListBuilder.create(),
                PartPose.offset(-0.0F, -0.0F, 0.0F));

        PartDefinition p_left_saddle_line_rot = p_left_rein3.addOrReplaceChild(
                "left_saddle_line_rot",
                CubeListBuilder.create()
                        .texOffs(0, 27).addBox(0.5000F, -1.5000F, -0.5000F, 0.0F, 3.0F, 16.0F),
                PartPose.offsetAndRotation(-0.5000F, 1.5000F, 0.5000F, -0.0F, 0.2182F, 0.0F));

        PartDefinition p_right_rein2 = p_snout2.addOrReplaceChild(
                "right_rein2",
                CubeListBuilder.create(),
                PartPose.offset(-2.5000F, 0.5000F, -2.5000F));

        PartDefinition p_right_rein3 = p_right_rein2.addOrReplaceChild(
                "right_rein3",
                CubeListBuilder.create(),
                PartPose.offset(-0.0F, -0.0F, 0.0F));

        PartDefinition p_right_saddle_line_rot = p_right_rein3.addOrReplaceChild(
                "right_saddle_line_rot",
                CubeListBuilder.create()
                        .texOffs(32, 27).addBox(-0.5000F, -1.5000F, -0.5000F, 0.0F, 3.0F, 16.0F),
                PartPose.offsetAndRotation(0.5000F, 1.5000F, 0.5000F, -0.0F, -0.2182F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
