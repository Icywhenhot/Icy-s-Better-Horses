package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IcysBetterHorses.RESOURCE_NAMESPACE);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IcysBetterHorses.RESOURCE_NAMESPACE);

    public static final RegistryObject<UpgradedSaddleItem> UPGRADED_SADDLE = ITEMS.register(
            "upgraded_saddle",
            () -> new UpgradedSaddleItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HORSE_HOOVES = ITEMS.register(
            "horse_hooves_gear",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HORSE_MEDKIT = ITEMS.register(
            "horse_medkit_gear",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CANISTER = ITEMS.register(
            "canister",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<BlockItem> HITCHPOST = ITEMS.register(
            "hitchpost",
            () -> new BlockItem(ModBlocks.HITCHPOST.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HORSE_STABILIZER = ITEMS.register(
            "horse_stabilizer_gear",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> STABLE_SUPPLIES_TAB = TABS.register("stable_supplies", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> UPGRADED_SADDLE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "stable_handbook"))
                                .filter(item -> item != Items.AIR)
                                .ifPresent(output::accept);
                        output.accept(UPGRADED_SADDLE.get());
                        output.accept(HORSE_HOOVES.get());
                        output.accept(HORSE_MEDKIT.get());
                        output.accept(CANISTER.get());
                        output.accept(HORSE_STABILIZER.get());
                        output.accept(HITCHPOST.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private ModItems() {}
}
