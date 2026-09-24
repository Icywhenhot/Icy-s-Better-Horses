package icy.betterhorses.net.inventory;

import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum GearSlot {
    CHEST,
    HOOVES,
    MEDKIT,
    STABILIZER;

    public static final int COUNT = values().length;

    private final TagKey<Item> items = TagKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID,
                    "gear/" + name().toLowerCase(java.util.Locale.ROOT)));

    public TagKey<Item> items() {
        return items;
    }

    public boolean accepts(ItemStack stack) {
        if (stack.isEmpty()) return true;
        if (stack.is(items)) return true;
        return switch (this) {
            case CHEST -> stack.is(Items.CHEST) || stack.is(Items.ENDER_CHEST);
            case HOOVES -> stack.is(ModItems.HORSE_HOOVES);
            case MEDKIT -> stack.is(ModItems.HORSE_MEDKIT);
            case STABILIZER -> stack.is(ModItems.HORSE_STABILIZER) || stack.is(ModItems.HORSE_CART);
        };
    }
}
