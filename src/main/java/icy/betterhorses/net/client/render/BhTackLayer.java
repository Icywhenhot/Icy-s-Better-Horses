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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class BhTackLayer<S extends BhHorseRenderState, M extends BhHorseModel>
        extends RenderLayer<S, M> {

    private static final int UNDYED_LEATHER = 0xBB744F;

    private final BhHorseModel adultModel;
    private final BhHorseModel babyModel;
    private final Function<S, Identifier> textureGetter;
    private final ToIntFunction<S> tint;

    public BhTackLayer(RenderLayerParent<S, M> parent,
                       BhHorseModel adultModel,
                       BhHorseModel babyModel,
                       Function<S, Identifier> textureGetter) {
        this(parent, adultModel, babyModel, textureGetter, state -> -1);
    }

    private BhTackLayer(RenderLayerParent<S, M> parent,
                        BhHorseModel adultModel,
                        BhHorseModel babyModel,
                        Function<S, Identifier> textureGetter,
                        ToIntFunction<S> tint) {
        super(parent);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        this.textureGetter = textureGetter;
        this.tint = tint;
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

    public static <S extends BhHorseRenderState, M extends BhHorseModel> BhTackLayer<S, M> forArmor(
            RenderLayerParent<S, M> parent,
            BhHorseModel adultModel,
            BhHorseModel babyModel,
            BhTackTextures tack) {
        return new BhTackLayer<>(parent, adultModel, babyModel,
                state -> {
                    ItemStack stack = state.bodyArmorItem;
                    return stack == null || stack.isEmpty() ? null : tack.armor(stack);
                },
                state -> {
                    ItemStack stack = state.bodyArmorItem;
                    if (stack == null || !stack.is(Items.LEATHER_HORSE_ARMOR)) {
                        return -1;
                    }
                    return 0xFF000000 | DyedItemColor.getOrDefault(stack, UNDYED_LEATHER);
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

        int color = tint.applyAsInt(state);

        if (opacity >= 1.0F) {
            renderColoredCutoutModel(model, texture, poseStack, collector, packedLight, state, color, 0);
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
                BhMountedHorseVisibility.applyOpacity(color, opacity),
                null,
                state.outlineColor,
                null);
    }
}
