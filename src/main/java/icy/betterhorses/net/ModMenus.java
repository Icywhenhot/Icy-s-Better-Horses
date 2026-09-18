package icy.betterhorses.net;

import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IcysBetterHorses.RESOURCE_NAMESPACE);

    public static final RegistryObject<MenuType<CartChestMenu>> CART_CHEST = MENUS.register("cart_chest",
            () -> new MenuType<>(CartChestMenu::new, FeatureFlags.VANILLA_SET));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }

    private ModMenus() {}
}
