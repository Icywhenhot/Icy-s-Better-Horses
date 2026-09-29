package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<CartMenu> CART = Registry.register(BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart"),
            new ExtendedMenuType<>(CartMenu::new, CartMenu.Opening.CODEC));

    private ModMenus() {}

    public static void init() {}
}
