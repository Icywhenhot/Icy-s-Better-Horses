package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IcysBetterHorses.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IcysBetterHorses.MOD_ID);

    public static final DeferredItem<UpgradedSaddleItem> UPGRADED_SADDLE = ITEMS.registerItem(
            "upgraded_saddle",
            UpgradedSaddleItem::new,
            properties -> properties.stacksTo(1).component(DataComponents.EQUIPPABLE, Equippable.saddle()));
    public static final DeferredItem<net.minecraft.world.item.Item> HORSE_HOOVES = ITEMS.registerSimpleItem(
            "horse_hooves_gear",
            properties -> properties.stacksTo(1).enchantable(15));
    public static final DeferredItem<net.minecraft.world.item.Item> HORSE_MEDKIT = ITEMS.registerSimpleItem(
            "horse_medkit_gear",
            properties -> properties.stacksTo(1));
    public static final DeferredItem<net.minecraft.world.item.Item> CANISTER = ITEMS.registerSimpleItem("canister");
    public static final DeferredItem<BlockItem> HITCHPOST = ITEMS.registerItem(
            "hitchpost",
            properties -> new BlockItem(ModBlocks.HITCHPOST.get(), properties.useBlockDescriptionPrefix().stacksTo(16)));
    public static final DeferredItem<net.minecraft.world.item.Item> HORSE_STABILIZER = ITEMS.registerSimpleItem(
            "horse_stabilizer_gear",
            properties -> properties.stacksTo(1));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STABLE_SUPPLIES_TAB = TABS.register("stable_supplies", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.icys_better_horses.stable_supplies"))
                    .icon(UPGRADED_SADDLE::toStack)
                    .displayItems((parameters, entries) -> {
                        BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_handbook"))
                                .filter(item -> item != Items.AIR)
                                .ifPresent(entries::accept);
                        entries.accept(UPGRADED_SADDLE);
                        entries.accept(HORSE_HOOVES);
                        entries.accept(HORSE_MEDKIT);
                        entries.accept(CANISTER);
                        entries.accept(HORSE_STABILIZER);
                        entries.accept(HITCHPOST);
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private ModItems() {}
}

