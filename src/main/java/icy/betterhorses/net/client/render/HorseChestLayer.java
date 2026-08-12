package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.mixin.HorseModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

// draws the donkey-style saddle pouch on the horse's flanks whenever a chest / ender chest
public final class HorseChestLayer<S extends EquineRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    private static final Identifier CHEST_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/horse/donkey.png");

    // same submit order vanilla uses for horse armor, after the base model, before name tags
    private static final int RENDER_ORDER = 2;

    private final ModelPart chest;

    public HorseChestLayer(RenderLayerParent<S, M> renderer) {
        super(renderer);
        this.chest = bakeChest();
    }

    private static ModelPart bakeChest() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder pouch = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);
        root.addOrReplaceChild(
                "left_chest", pouch, PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, (float) (-Math.PI / 2), 0.0F));
        root.addOrReplaceChild(
                "right_chest", pouch, PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, (float) (Math.PI / 2), 0.0F));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void submit(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            S state,
            float yRot,
            float xRot) {
        if (!((IBhEquineStabilizerState) (Object) state).bh_hasChestGear() || state.isInvisible || state.isBaby) {
            return;
        }
        // donkeys and mules already wear the vanilla pouch when they're carrying a chest
        if (state instanceof DonkeyRenderState donkey && donkey.hasChest) {
            return;
        }
        if (!(this.getParentModel() instanceof HorseModelAccessor parentModel)) {
            return;
        }
        // A breed with its own chest model draws it in its own renderer, on its own animator.
        // Without this the generic donkey pouch renders on top of it. Keyed off the shared
        // base rather than a list of breed classes, so a new breed is covered automatically.
        if (this.getParentModel() instanceof BhHorseModel) {
            return;
        }

        float opacity = BhRenderContext.currentOpacity();
        if (opacity <= 0.01F) {
            return;
        }

        RenderType renderType = opacity < 1.0F
                ? RenderTypes.entityTranslucent(CHEST_TEXTURE)
                : RenderTypes.entityCutout(CHEST_TEXTURE);

        poseStack.pushPose();
        this.getParentModel().root().translateAndRotate(poseStack);
        parentModel.bh_getBody().translateAndRotate(poseStack);
        collector.order(RENDER_ORDER).submitModelPart(
                this.chest,
                poseStack,
                renderType,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                null,
                BhMountedHorseVisibility.applyOpacity(-1, opacity),
                null,
                state.outlineColor);
        poseStack.popPose();
    }
}
