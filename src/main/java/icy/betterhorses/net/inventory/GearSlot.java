package icy.betterhorses.net.inventory;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum GearSlot {
    CHEST,
    HOOVES,
    MEDKIT,
    STABILIZER,
    HITCHPOST;

    public static final int COUNT = values().length;

    public boolean accepts(ItemStack stack) {
        if (stack.isEmpty()) return true;
        return switch (this) {
            case CHEST -> stack.is(Items.CHEST);
            case HOOVES -> stack.is(ModItems.HORSE_HOOVES.get());
            case MEDKIT -> stack.is(ModItems.HORSE_MEDKIT.get());
            case STABILIZER -> stack.is(ModItems.HORSE_STABILIZER.get());
            case HITCHPOST -> stack.is(ModItems.HITCHPOST.get());
        };
    }

    public String translationKey() {
        return "gear.icys-better-horses." + name().toLowerCase();
    }
}
