package icy.betterhorses.net;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;

public final class BhBanners {

    private BhBanners() {}

    public static boolean fits(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || heralds(stack);
    }

    public static boolean heralds(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("heralds_luna") && id.getPath().startsWith("banner_");
    }
}
