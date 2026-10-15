package icy.betterhorses.net.inventory;

import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.WoolCarpetBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum GearSlot {
    CHEST,
    HOOVES,
    MEDKIT,
    STABILIZER,
    CARPET;

    public static final int COUNT = values().length;

    private final TagKey<Item> items = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE,
                    "gear/" + name().toLowerCase(java.util.Locale.ROOT)));

    public TagKey<Item> items() {
        return items;
    }

    public boolean enabled() {
        return switch (this) {
            case HOOVES -> BhConfig.hoovesEnabled();
            case MEDKIT -> BhConfig.medkitEnabled();
            case CHEST, STABILIZER, CARPET -> true;
        };
    }

    public static boolean isStorageChest(ItemStack stack) {
        return stack.is(CHEST.items);
    }

    public static boolean isEnderChest(ItemStack stack) {
        return stack.is(Items.ENDER_CHEST) || BhEnderStorage.isChest(stack);
    }

    public static boolean isChest(ItemStack stack) {
        return isStorageChest(stack) || isEnderChest(stack);
    }

    public static @Nullable DyeColor carpetColor(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof WoolCarpetBlock carpet
                ? carpet.getColor()
                : null;
    }

    public boolean accepts(ItemStack stack) {
        if (!enabled()) return false;
        if (stack.isEmpty()) return true;
        if (this == CARPET) return carpetColor(stack) != null;
        if (stack.is(ModItems.HORSE_STABILIZER) && !BhConfig.stabilizerEnabled()) return false;
        if (stack.is(items)) return true;
        return switch (this) {
            case CHEST -> isEnderChest(stack);
            case HOOVES -> stack.is(ModItems.HORSE_HOOVES);
            case MEDKIT -> stack.is(ModItems.HORSE_MEDKIT);
            case STABILIZER -> stack.is(ModItems.HORSE_STABILIZER) || stack.is(ModItems.HORSE_CART);
            case CARPET -> false;
        };
    }
}
