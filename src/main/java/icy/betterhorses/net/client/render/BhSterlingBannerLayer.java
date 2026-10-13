package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.item.ItemStack;
import net.orcinus.galosphere.api.SterlingAttachable;

public class BhSterlingBannerLayer<T extends BhBreedHorse> extends BhBannerLayer<T> {

    public BhSterlingBannerLayer(RenderLayerParent<T, BhHorseModel<T>> parent) {
        super(parent);
    }

    @Override
    protected ItemStack banner(T entity, ItemStack armor) {
        ItemStack own = super.banner(entity, armor);
        if (!own.isEmpty() || !(armor.getItem() instanceof SterlingAttachable sterling)) {
            return own;
        }
        return sterling.getAttached(armor);
    }
}
