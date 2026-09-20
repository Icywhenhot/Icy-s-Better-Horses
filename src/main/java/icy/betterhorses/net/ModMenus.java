package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartChestMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    // MenuType's constructor is private on Fabric, so register through Fabric API's registerSimple().
    // It's deprecated, but avoids needing an access widener.
    public static final MenuType<CartChestMenu> CART_CHEST = ScreenHandlerRegistry.registerSimple(
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "cart_chest"),
            CartChestMenu::new);

    public static void register() {}

    private ModMenus() {}
}
