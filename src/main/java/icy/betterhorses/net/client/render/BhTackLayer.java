package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public class BhTackLayer<T extends BhBreedHorse> extends RenderLayer<T, BhHorseModel<T>> {

    private final BhHorseModel<T> adultModel;
    private final BhHorseModel<T> babyModel;
    private final Function<T, ResourceLocation> textureGetter;
    private final ToIntFunction<T> tint;

    public BhTackLayer(RenderLayerParent<T, BhHorseModel<T>> parent,
                       BhHorseModel<T> adultModel,
                       BhHorseModel<T> babyModel,
                       Function<T, ResourceLocation> textureGetter) {
        this(parent, adultModel, babyModel, textureGetter, entity -> -1);
    }

    public BhTackLayer(RenderLayerParent<T, BhHorseModel<T>> parent,
                       BhHorseModel<T> adultModel,
                       BhHorseModel<T> babyModel,
                       Function<T, ResourceLocation> textureGetter,
                       ToIntFunction<T> tint) {
        super(parent);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        this.textureGetter = textureGetter;
        this.tint = tint;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation texture = textureGetter.apply(entity);
        if (texture == null || entity.isInvisible()) return;

        BhHorseModel<T> model = entity.isBaby() ? babyModel : adultModel;
        getParentModel().copyPropertiesTo(model);
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float opacity = BhMountedHorseVisibility.currentOpacity();
        if (opacity <= 0.01F) return;
        RenderType renderType = opacity < 1.0F
                ? RenderType.entityTranslucent(texture)
                : RenderType.entityCutoutNoCull(texture);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        int color = tint.applyAsInt(entity);
        model.renderToBuffer(poseStack, consumer, packedLight,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                BhMountedHorseVisibility.red(color),
                BhMountedHorseVisibility.green(color),
                BhMountedHorseVisibility.blue(color),
                BhMountedHorseVisibility.alpha(color, opacity));
    }
}
