package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometry for the Icelandic horse's armor.
 *
 * <p>GENERATED from
 * {@code blockbench/horses/icelandic/Armor.bbmodel}
 * via the Fresh-Animations rig. Do not hand-edit: re-run the generator instead, which
 * verifies every cube round-trips back to the Blockbench source.
 *
 * <p>The part names deliberately mirror the CEM rig rather than vanilla's, because
 * the animator drives a finer skeleton than vanilla exposes: vanilla lumps head,
 * ears, mouth, neck and mane into one swivelling container, which cannot express an
 * arching neck or a muzzle that chews independently.
 */
public final class IcelandicArmorGeometry {

    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private IcelandicArmorGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -5.0F, -11.0F, 8.0F, 9.0F, 18.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, 9.0F, 0.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, -9.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(0, 27).addBox(-2.0F, -11.0F, -3.0F, 4.0F, 12.0F, 6.0F, new CubeDeformation(0.0990F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(20, 27).addBox(-2.5000F, -5.0F, -4.0F, 5.0F, 5.0F, 7.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, -8.0F, -1.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(44, 27).addBox(-1.5000F, -1.0F, -5.0F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0600F)),
                PartPose.offset(0.0F, -3.0F, -4.0F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create(),
                PartPose.offset(1.0F, -5.0F, 2.9900F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create(),
                PartPose.offset(-1.0F, -5.0F, 2.9900F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, 3.0F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -1.0F, 3.0F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, 7.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .texOffs(32, 39).addBox(-1.5000F, -10.0F, -1.5000F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0800F)),
                PartPose.offset(-2.5000F, 23.0F, -9.4000F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(20, 39).addBox(-1.5000F, -10.0F, -1.5000F, 3.0F, 11.0F, 3.0F, new CubeDeformation(0.0800F)),
                PartPose.offset(2.5000F, 23.0F, -9.4000F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
