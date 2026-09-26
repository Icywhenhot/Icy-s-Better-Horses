package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<CartChestMenu> CART_CHEST = Registry.register(
            BuiltInRegistries.MENU,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "cart_chest"),
            new MenuType<>(CartChestMenu::new, FeatureFlags.VANILLA_SET));

    public static void register() {}

    private ModMenus() {}
}
