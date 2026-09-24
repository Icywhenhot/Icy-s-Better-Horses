package icy.betterhorses.net.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import icy.betterhorses.net.entity.BhBreedHorse;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.function.Function;

public class BhHorseRenderer<T extends BhBreedHorse> extends MobRenderer<T, BhHorseModel<T>> {

    private final BhHorseModel<T> adultModel;
    private final BhHorseModel<T> babyModel;

    public BhHorseRenderer(EntityRendererProvider.Context context,
                           BhHorseModel<T> adultModel,
                           BhHorseModel<T> babyModel) {
        super(context, adultModel, 0.75F);
        this.adultModel = adultModel;
        this.babyModel = babyModel;
    }

    protected BhHorseRenderer(EntityRendererProvider.Context context,
                              BhHorseModel<T> adultModel,
                              BhHorseModel<T> babyModel,
                              Function<ModelLayerLocation, BhHorseModel<T>> models,
                              ModelLayerLocation saddle,
                              ModelLayerLocation saddleBaby,
                              ModelLayerLocation armor,
                              ModelLayerLocation armorBaby,
                              ModelLayerLocation chest,
                              ModelLayerLocation chestBaby,
                              BhTackTextures textures) {
        this(context, adultModel, babyModel);
        addLayer(new BhTackLayer<>(this, models.apply(saddle), models.apply(saddleBaby), entity ->
                entity.isSaddled()
                        ? textures.saddle(IHorseData.of(entity).bh_hasUpgradedSaddle())
                        : null));
        addLayer(new BhTackLayer<>(this, models.apply(armor), models.apply(armorBaby), entity -> {
            ItemStack stack = entity.getBodyArmorItem();
            return stack.isEmpty() ? null : textures.armor(stack);
        }, entity -> {
            ItemStack stack = entity.getBodyArmorItem();
            if (stack.is(Items.LEATHER_HORSE_ARMOR)) {
                return 0xFF000000 | DyedItemColor.getOrDefault(stack, 0xBB744F);
            }
            DyedItemColor dye = stack.get(DataComponents.DYED_COLOR);
            return dye == null ? -1 : 0xFF000000 | dye.rgb();
        }));
        addLayer(new BhTackLayer<>(this, models.apply(chest), models.apply(chestBaby), entity -> {
            IHorseData data = IHorseData.of(entity);
            return data.bh_hasGear(GearSlot.CHEST) ? textures.chest(data.bh_hasEnderChestGear()) : null;
        }));
        addLayer(new HorseStabilizerLayer<>(this));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        model = entity.isBaby() ? babyModel : adultModel;
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        if (IHorseData.of(entity).bh_hasCartGear() && entity.getControllingPassenger() != null) {
            BhCartReins.render(entity, partialTick, poseStack, buffer);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return BhNamedCoats.coat(entity);
    }
}
