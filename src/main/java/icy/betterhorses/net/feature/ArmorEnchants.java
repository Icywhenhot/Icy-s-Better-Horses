package icy.betterhorses.net.feature;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ArmorEnchants {

    private ArmorEnchants() {}

    public static boolean accepts(Enchantment enchantment) {
        if (enchantment == Enchantments.AQUA_AFFINITY) return false;
        return accepts(enchantment.category)
                || enchantment == Enchantments.BINDING_CURSE
                || enchantment == Enchantments.VANISHING_CURSE;
    }

    public static boolean accepts(EnchantmentCategory category) {
        return category == EnchantmentCategory.ARMOR
                || category == EnchantmentCategory.ARMOR_CHEST
                || category == EnchantmentCategory.ARMOR_FEET
                || category == EnchantmentCategory.ARMOR_HEAD
                || category == EnchantmentCategory.VANISHABLE;
    }
}
