package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(IcysBetterHorses.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IcysBetterHorses.MOD_ID);

    public static final Item UPGRADED_SADDLE = new UpgradedSaddleItem(itemProperties("upgraded_saddle")
            .stacksTo(1)
            .component(DataComponents.EQUIPPABLE, Equippable.saddle()));
    public static final Item HORSE_HOOVES = new Item(itemProperties("horse_hooves_gear").stacksTo(1).enchantable(15));
    public static final Item HORSE_MEDKIT = new Item(itemProperties("horse_medkit_gear").stacksTo(1));
    public static final Item CANISTER = new Item(itemProperties("canister"));
    public static final Item HITCHPOST = new BlockItem(ModBlocks.HITCHPOST, blockItemProperties("hitchpost").stacksTo(16));
    public static final Item HORSE_STABILIZER = new Item(itemProperties("horse_stabilizer_gear").stacksTo(1));

    public static final CreativeModeTab STABLE_SUPPLIES_TAB = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.icys_better_horses.stable_supplies"))
            .icon(() -> new ItemStack(UPGRADED_SADDLE))
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
            .build();

    @SuppressWarnings("unused") private static final DeferredHolder<Item, Item> UPGRADED_SADDLE_ENTRY = ITEMS.register("upgraded_saddle", () -> UPGRADED_SADDLE);
    @SuppressWarnings("unused") private static final DeferredHolder<Item, Item> HORSE_HOOVES_ENTRY = ITEMS.register("horse_hooves_gear", () -> HORSE_HOOVES);
    @SuppressWarnings("unused") private static final DeferredHolder<Item, Item> HORSE_MEDKIT_ENTRY = ITEMS.register("horse_medkit_gear", () -> HORSE_MEDKIT);
    @SuppressWarnings("unused") private static final DeferredHolder<Item, Item> CANISTER_ENTRY = ITEMS.register("canister", () -> CANISTER);
    @SuppressWarnings("unused") private static final DeferredHolder<Item, Item> HITCHPOST_ENTRY = ITEMS.register("hitchpost", () -> HITCHPOST);
    @SuppressWarnings("unused") private static final DeferredHolder<Item, Item> HORSE_STABILIZER_ENTRY = ITEMS.register("horse_stabilizer_gear", () -> HORSE_STABILIZER);
    @SuppressWarnings("unused") private static final DeferredHolder<CreativeModeTab, CreativeModeTab> STABLE_SUPPLIES_TAB_ENTRY = TABS.register("stable_supplies", () -> STABLE_SUPPLIES_TAB);

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private static Item.Properties itemProperties(String path) {
        return new Item.Properties().setId(itemKey(path));
    }

    private static Item.Properties blockItemProperties(String path) {
        return itemProperties(path).useBlockDescriptionPrefix();
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path));
    }

    private ModItems() {}
}

