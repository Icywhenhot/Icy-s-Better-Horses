package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Tack textures for the medium size class, shared by every breed in it.
 *
 * <p>Unlike the Icelandic's and the Friesian's, these live in a {@code medium/} folder
 * rather than a breed folder — a saddle that fits one medium horse fits all of them,
 * because they are the same mesh. Coats still come from the breed's own folder.
 *
 * <p>Vanilla's equipment-asset system is bypassed entirely here, because it paints our
 * custom geometry with vanilla textures at vanilla UVs.
 */
public final class MediumTackTextures {

    private static final String BASE = "textures/entity/horse/medium/";

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

    private MediumTackTextures() {}

    private static Identifier tex(String name) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, BASE + name + ".png");
    }

    /**
     * The pannier drawn when the chest gear is equipped. The slot takes either a chest or an
     * ender chest, and the two read very differently in the world, so they get their own hides.
     */
    public static Identifier chest(boolean ender) {
        return ender ? ENDER_CHEST : CHEST;
    }

    /** The mod's upgraded saddle gets its own look; anything else saddle-shaped uses the plain one. */
    public static Identifier saddle(ItemStack stack) {
        return stack.is(ModItems.UPGRADED_SADDLE) ? SADDLE_UPGRADED : SADDLE;
    }

    /**
     * All six vanilla tiers have their own medium texture, so nothing falls back.
     * Iron remains the default for any tier a future Minecraft version adds.
     */
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
