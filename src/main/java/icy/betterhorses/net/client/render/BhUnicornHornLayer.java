package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamabnormals.blueprint.common.world.storage.tracking.IDataManager;
import com.teamabnormals.caverns_and_chasms.core.other.CCDataProcessors;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.IdentityHashMap;
import java.util.Map;

public class BhUnicornHornLayer<T extends BhBreedHorse> extends RenderLayer<T, BhHorseModel<T>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "textures/entity/horse/unicorn_horn.png");

    private final Map<ModelPart, ModelPart> horns = new IdentityHashMap<>();

    public BhUnicornHornLayer(RenderLayerParent<T, BhHorseModel<T>> parent) {
        super(parent);
    }

    private static ModelPart hornFor(ModelPart head) {
        float[] box = {0.0F, -4.0F};
        head.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                box[0] = cube.minY;
                box[1] = cube.minZ;
            }
        });
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("horn",
                CubeListBuilder.create().texOffs(0, 57).addBox(-0.5F, box[0] - 6.0F, box[1] + 2.5F, 1.0F, 6.0F, 1.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) return;
        IDataManager data = (IDataManager) entity;
        ItemStack horn = data.getValue(CCDataProcessors.UNICORN_HORN);
        if (horn.isEmpty()) return;
        float opacity = BhMountedHorseVisibility.currentOpacity();
        if (opacity <= 0.01F) return;

        BhHorseModel<T> model = getParentModel();
        ModelPart part = horns.computeIfAbsent(model.head(), BhUnicornHornLayer::hornFor);
        int color = horn.is(ItemTags.DYEABLE) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(horn, -6265536)) : -1;
        color = BhMountedHorseVisibility.applyOpacity(color, opacity);

        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        model.bh_getBody().translateAndRotate(poseStack);
        model.neck().translateAndRotate(poseStack);
        model.head().translateAndRotate(poseStack);
        if (BhNeckPlates.of(entity.getBodyArmorItem()) != null) {
            poseStack.translate(0.0F, -6.0F / 16.0F, 0.0F);
        }
        RenderType type = opacity < 1.0F ? RenderType.entityTranslucent(TEXTURE) : RenderType.entityCutoutNoCull(TEXTURE);
        part.render(poseStack, buffer.getBuffer(type), packedLight, OverlayTexture.NO_OVERLAY, color);
        if (data.getValue(CCDataProcessors.GLOW_UNICORN_HORN)) {
            part.render(poseStack, buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE)),
                    packedLight, OverlayTexture.NO_OVERLAY, color);
        }
        poseStack.popPose();
    }
}
