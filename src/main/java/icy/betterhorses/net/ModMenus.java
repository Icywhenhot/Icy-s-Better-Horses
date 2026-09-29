package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IcysBetterHorses.RESOURCE_NAMESPACE);

    public static final RegistryObject<MenuType<CartMenu>> CART = MENUS.register("cart",
            () -> IForgeMenuType.create(CartMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    private ModMenus() {}
}
