package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

public final class ModItems {

    public static final Item UPGRADED_SADDLE = register("upgraded_saddle",
            new UpgradedSaddleItem(itemProperties("upgraded_saddle")
                    .stacksTo(1)
                    .component(DataComponents.EQUIPPABLE, Equippable.saddle())));

    public static final Item HORSE_HOOVES = register("horse_hooves_gear",
            new Item(itemProperties("horse_hooves_gear").stacksTo(1).enchantable(15)));

    // Keep the original medkit id so existing worlds keep their saved item stacks.
    public static final Item HORSE_MEDKIT = register("horse_medkit_gear",
            new Item(itemProperties("horse_medkit_gear").stacksTo(1)));

    public static final Item CANISTER = register("canister",
            new Item(itemProperties("canister")));

    public static final Item HITCHPOST = register("hitchpost",
            new BlockItem(ModBlocks.HITCHPOST, blockItemProperties("hitchpost").stacksTo(16)));

    public static final Item HORSE_STABILIZER = register("horse_stabilizer_gear",
            new Item(itemProperties("horse_stabilizer_gear").stacksTo(1)));

    public static final CreativeModeTab STABLE_SUPPLIES_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_supplies"),
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> new ItemStack(UPGRADED_SADDLE))
                    .displayItems((parameters, entries) -> {
                        BuiltInRegistries.ITEM
                                .get(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_handbook"))
                                .map(net.minecraft.core.Holder::value)
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

    public static void init() {
    }

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                item);
    }

    private static Item.Properties itemProperties(String path) {
        return new Item.Properties().setId(itemKey(path));
    }

    private static Item.Properties blockItemProperties(String path) {
        return itemProperties(path).useBlockDescriptionPrefix();
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path));
    }

    private ModItems() {}
}
