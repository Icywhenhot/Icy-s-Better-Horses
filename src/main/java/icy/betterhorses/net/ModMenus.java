package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<CartChestMenu> CART_CHEST = register("cart_chest",
            new MenuType<>(CartChestMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {}

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> register(
            String name, MenuType<T> type) {
        return Registry.register(BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, name), type);
    }

    public static void init() {}
}
