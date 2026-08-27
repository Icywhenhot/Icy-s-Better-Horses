package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BhTackTextures {

    public static final BhTackTextures BELGIAN = new BhTackTextures("belgian");
    public static final BhTackTextures FRIESIAN = new BhTackTextures("friesian");
    public static final BhTackTextures ICELANDIC = new BhTackTextures("icelandic");
    public static final BhTackTextures MEDIUM = new BhTackTextures("medium");
    public static final BhTackTextures PERCHERON = new BhTackTextures("percheron");
    public static final BhTackTextures SHIRE = new BhTackTextures("shire");

    private final Identifier saddle;
    private final Identifier saddleUpgraded;
    private final Identifier chest;
    private final Identifier enderChest;

    private final Identifier armorLeather;
    private final Identifier armorCopper;
    private final Identifier armorIron;
    private final Identifier armorGold;
    private final Identifier armorDiamond;
    private final Identifier armorNetherite;

    private BhTackTextures(String breed) {
        String base = "textures/entity/horse/" + breed + "/";
        this.saddle = tex(base, "saddle");
        this.saddleUpgraded = tex(base, "saddle_upgraded");
        this.chest = tex(base, "chest");
        this.enderChest = tex(base, "ender_chest");
        this.armorLeather = tex(base, "armor_leather");
        this.armorCopper = tex(base, "armor_copper");
        this.armorIron = tex(base, "armor_iron");
        this.armorGold = tex(base, "armor_gold");
        this.armorDiamond = tex(base, "armor_diamond");
        this.armorNetherite = tex(base, "armor_netherite");
    }

    private static Identifier tex(String base, String name) {
        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, base + name + ".png");
    }

    public Identifier chest(boolean ender) {
        return ender ? enderChest : chest;
    }

    public Identifier saddle(ItemStack stack) {
        return stack.is(ModItems.UPGRADED_SADDLE) ? saddleUpgraded : saddle;
    }

    public Identifier armor(ItemStack stack) {
        if (stack.is(Items.LEATHER_HORSE_ARMOR)) {
            return armorLeather;
        }
        if (stack.is(Items.COPPER_HORSE_ARMOR)) {
            return armorCopper;
        }
        if (stack.is(Items.GOLDEN_HORSE_ARMOR)) {
            return armorGold;
        }
        if (stack.is(Items.DIAMOND_HORSE_ARMOR)) {
            return armorDiamond;
        }
        if (stack.is(Items.NETHERITE_HORSE_ARMOR)) {
            return armorNetherite;
        }
        return armorIron;
    }
}
