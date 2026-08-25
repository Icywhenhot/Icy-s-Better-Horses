package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class BhTackLayer<S extends BhHorseRenderState, M extends BhHorseModel>
        extends RenderLayer<S, M> {

    private final BhHorseModel adultModel;
    private final BhHorseModel babyModel;
    private final Function<S, Identifier> textureGetter;

    public BhTackLayer(RenderLayerParent<S, M> parent,
                       BhHorseModel adultModel,
                       BhHorseModel babyModel,
                       Function<S, Identifier> textureGetter) {
        super(parent);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        this.textureGetter = textureGetter;
    }

    public static <S extends BhHorseRenderState, M extends BhHorseModel> BhTackLayer<S, M> forItem(
            RenderLayerParent<S, M> parent,
            BhHorseModel adultModel,
            BhHorseModel babyModel,
            Function<S, ItemStack> itemGetter,
            Function<ItemStack, Identifier> itemTexture) {
        return new BhTackLayer<>(parent, adultModel, babyModel, state -> {
            ItemStack stack = itemGetter.apply(state);
            return stack == null || stack.isEmpty() ? null : itemTexture.apply(stack);
        });
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       S state, float yRot, float xRot) {
        Identifier texture = textureGetter.apply(state);
        if (texture == null) {
            return;
        }

        float opacity = BhRenderContext.currentOpacity();
        if (opacity <= 0.01F) {
            return;
        }

        BhHorseModel model = state.isBaby ? babyModel : adultModel;
        model.setupAnim(state);

        if (opacity >= 1.0F) {
            renderColoredCutoutModel(model, texture, poseStack, collector, packedLight, state, -1, 0);
            return;
        }

        RenderType renderType = RenderTypes.entityTranslucent(texture);
        collector.submitModel(
                model,
                state,
                poseStack,
                renderType,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                BhMountedHorseVisibility.applyOpacity(-1, opacity),
                null,
                state.outlineColor,
                null);
    }
}
