package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<CartMenu> CART = Registry.register(BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "cart"),
            new ExtendedScreenHandlerType<>(CartMenu::new, CartMenu.Opening.CODEC));

    private ModMenus() {}

    public static void init() {}
}
