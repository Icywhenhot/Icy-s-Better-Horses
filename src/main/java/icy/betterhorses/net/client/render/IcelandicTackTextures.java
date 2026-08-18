package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IcelandicTackTextures {

    private static final String BASE = "textures/entity/horse/icelandic/";

    private static final Identifier SADDLE = tex("saddle");
    private static final Identifier SADDLE_UPGRADED = tex("saddle_upgraded");
    private static final Identifier CHEST = tex("chest");
    private static final Identifier ENDER_CHEST = tex("ender_chest");

    private static final Identifier ARMOR_LEATHER = tex("armor_leather");
    private static final Identifier ARMOR_COPPER = tex("armor_copper");
    private static final Identifier ARMOR_IRON = tex("armor_iron");
    private static final Identifier ARMOR_GOLD = tex("armor_gold");
    private static final Identifier ARMOR_DIAMOND = tex("armor_diamond");
    private static final Identifier ARMOR_NETHERITE = tex("armor_netherite");

    private IcelandicTackTextures() {}

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, BASE + name + ".png");
    }

    public static Identifier chest(boolean ender) {
        return ender ? ENDER_CHEST : CHEST;
    }

    public static Identifier saddle(ItemStack stack) {
        return stack.is(ModItems.UPGRADED_SADDLE) ? SADDLE_UPGRADED : SADDLE;
    }

    public static Identifier armor(ItemStack stack) {
        if (stack.is(Items.LEATHER_HORSE_ARMOR)) {
            return ARMOR_LEATHER;
        }
        if (stack.is(Items.COPPER_HORSE_ARMOR)) {
            return ARMOR_COPPER;
        }
        if (stack.is(Items.GOLDEN_HORSE_ARMOR)) {
            return ARMOR_GOLD;
        }
        if (stack.is(Items.DIAMOND_HORSE_ARMOR)) {
            return ARMOR_DIAMOND;
        }
        if (stack.is(Items.NETHERITE_HORSE_ARMOR)) {
            return ARMOR_NETHERITE;
        }
        return ARMOR_IRON;
    }
}
