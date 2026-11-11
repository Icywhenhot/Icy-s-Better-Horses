package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class AsinaraDonkeyArmorGeometry {

    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private AsinaraDonkeyArmorGeometry() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition p_body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(9, 7).addBox(-4.0F, -4.9500F, -11.0F, 8.0F, 8.0F, 16.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition p_neck2 = p_body.addOrReplaceChild(
                "neck2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -2.0F, -7.0F));

        PartDefinition p_neck3 = p_neck2.addOrReplaceChild(
                "neck3",
                CubeListBuilder.create()
                        .texOffs(11, 76).addBox(-2.0F, -8.0F, -3.9000F, 4.0F, 11.0F, 6.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition p_head2 = p_neck2.addOrReplaceChild(
                "head2",
                CubeListBuilder.create()
                        .texOffs(69, 65).addBox(-2.5000F, -5.0F, -3.9000F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.1000F)),
                PartPose.offset(0.0F, -8.0F, 0.0F));

        PartDefinition p_snout2 = p_head2.addOrReplaceChild(
                "snout2",
                CubeListBuilder.create()
                        .texOffs(101, 65).addBox(-1.5000F, -1.0F, -5.0833F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0600F)),
                PartPose.offset(0.0F, -4.0F, -3.9000F));

        PartDefinition p_left_ear2 = p_head2.addOrReplaceChild(
                "left_ear2",
                CubeListBuilder.create(),
                PartPose.offset(1.0F, -4.0F, 2.0900F));

        PartDefinition p_right_ear2 = p_head2.addOrReplaceChild(
                "right_ear2",
                CubeListBuilder.create(),
                PartPose.offset(-1.0F, -4.0F, 2.0900F));

        PartDefinition p_mane3 = p_head2.addOrReplaceChild(
                "mane3",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -3.0F, 2.1000F));

        PartDefinition p_mane2 = p_neck2.addOrReplaceChild(
                "mane2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 2.1000F));

        PartDefinition p_tail2 = p_body.addOrReplaceChild(
                "tail2",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -5.0F, 5.0F));

        PartDefinition p_front_right_leg = root.addOrReplaceChild(
                "front_right_leg",
                CubeListBuilder.create()
                        .mirror().texOffs(57, 0).addBox(-1.5000F, -7.8846F, -1.5000F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0800F)),
                PartPose.offset(-2.5000F, 23.0F, -9.4000F));

        PartDefinition p_front_left_leg = root.addOrReplaceChild(
                "front_left_leg",
                CubeListBuilder.create()
                        .texOffs(62, 80).addBox(-1.5000F, -7.8846F, -1.5000F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0800F)),
                PartPose.offset(2.5000F, 23.0F, -9.4000F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
