package icy.betterhorses.net.feature;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public final class ArmorEnchants {

    private ArmorEnchants() {}

    public static boolean accepts(Enchantment enchantment) {
        if (enchantment == Enchantments.AQUA_AFFINITY) return false;
        return enchantment.category == EnchantmentCategory.ARMOR
                || enchantment.category == EnchantmentCategory.ARMOR_CHEST
                || enchantment.category == EnchantmentCategory.ARMOR_FEET
                || enchantment.category == EnchantmentCategory.ARMOR_HEAD
                || enchantment == Enchantments.BINDING_CURSE
                || enchantment == Enchantments.VANISHING_CURSE;
    }
}
