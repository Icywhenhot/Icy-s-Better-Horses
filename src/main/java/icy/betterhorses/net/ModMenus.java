package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModMenus {

    public static final MenuType<CartChestMenu> CART_CHEST = register("cart_chest",
            new MenuType<>(CartChestMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {}

    private static <T extends net.minecraft.world.inventory.AbstractContainerMenu> MenuType<T> register(
            String name, MenuType<T> type) {
        return type;
    }

    public static void register(RegisterEvent event) {
        event.register(
                Registries.MENU,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "cart_chest"),
                () -> CART_CHEST);
    }
}
