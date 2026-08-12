package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * Draws a piece of tack on a breed horse using our own model and our own texture.
 *
 * <p>This exists because {@code SimpleEquipmentLayer} cannot be told which texture to use.
 * It resolves one through vanilla's equipment-asset system: a vanilla saddle item points at
 * {@code minecraft:saddle}, which maps to {@code entity/equipment/horse_saddle/saddle.png}.
 * Handed our custom geometry that produced garbage, because the texture and the UVs came from
 * two different models.
 *
 * <p>The model passed in is a {@link BhHorseModel} — for a given breed, the same class as
 * that breed's horse body — so the tack runs the identical animator and cannot drift out of
 * step with the barrel it is strapped to.
 *
 * @param <M> the breed's model class, so a Friesian renderer cannot be handed Icelandic tack
 */
public class BhTackLayer<S extends BhHorseRenderState, M extends BhHorseModel>
        extends RenderLayer<S, M> {

    private final BhHorseModel adultModel;
    private final BhHorseModel babyModel;
    private final Function<S, Identifier> textureGetter;

    /**
     * General form: pick a texture from the render state, or return {@code null} to draw nothing.
     * Used directly for gear that is a flag rather than an item, like the chest.
     */
    public BhTackLayer(RenderLayerParent<S, M> parent,
                       BhHorseModel adultModel,
                       BhHorseModel babyModel,
                       Function<S, Identifier> textureGetter) {
        super(parent);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        this.textureGetter = textureGetter;
    }

    /**
     * Equipment form, for tack backed by an {@link ItemStack} on the render state.
     *
     * @param itemGetter    pulls the relevant stack off the render state (saddle, body armour)
     * @param itemTexture   chooses a texture for that stack, or {@code null} to draw nothing
     */
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

        BhHorseModel model = state.isBaby ? babyModel : adultModel;
        // pose the tack from the same state the body used, so every bone lines up
        model.setupAnim(state);
        renderColoredCutoutModel(model, texture, poseStack, collector, packedLight, state, -1, 0);
    }
}
