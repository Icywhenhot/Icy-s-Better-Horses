package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockItem;
import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItems {

    public static final Item UPGRADED_SADDLE = register("upgraded_saddle",
            new UpgradedSaddleItem(new Item.Properties().stacksTo(1)));

    // Legacy compatibility item. New horse chest storage uses the vanilla chest item directly.
    public static final Item HORSE_CHEST_GEAR = register("horse_chest_gear",
            new Item(new Item.Properties().stacksTo(1)));

    public static final Item HORSE_HOOVES = register("horse_hooves_gear",
            new Item(new Item.Properties().stacksTo(1)));

    // Keep the original medkit id so existing worlds keep their saved item stacks.
    public static final Item HORSE_MEDKIT = register("horse_medkit_gear",
            new Item(new Item.Properties().stacksTo(1)));

    public static final Item HITCHPOST = register("hitchpost",
            new HitchpostBlockItem(new Item.Properties().stacksTo(16)));

    public static final Item HORSE_STABILIZER = register("horse_stabilizer_gear",
            new Item(new Item.Properties().stacksTo(1)));

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.addAfter(Items.SADDLE, UPGRADED_SADDLE);
            entries.addAfter(UPGRADED_SADDLE,
                    HORSE_HOOVES, HORSE_MEDKIT, HORSE_STABILIZER, HITCHPOST);
        });
    }

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                item);
    }

    private ModItems() {}
}
