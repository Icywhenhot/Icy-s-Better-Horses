package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModMenus {

    public static final MenuType<CartMenu> CART = IMenuTypeExtension.create(CartMenu::new);

    private ModMenus() {}

    public static void register(RegisterEvent event) {
        event.register(
                Registries.MENU,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "cart"),
                () -> CART);
    }
}
