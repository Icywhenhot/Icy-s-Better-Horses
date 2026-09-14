package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BhTackTextures {

    public static final BhTackTextures BELGIAN = new BhTackTextures("belgian");
    public static final BhTackTextures FRIESIAN = new BhTackTextures("friesian");
    public static final BhTackTextures HAFLINGER = new BhTackTextures("haflinger");
    public static final BhTackTextures ICELANDIC = new BhTackTextures("icelandic");
    public static final BhTackTextures MEDIUM = new BhTackTextures("medium");
    public static final BhTackTextures PERCHERON = new BhTackTextures("percheron");
    public static final BhTackTextures SHIRE = new BhTackTextures("shire");
    public static final BhTackTextures SMALL = new BhTackTextures("small");

    private final ResourceLocation saddle;
    private final ResourceLocation saddleUpgraded;
    private final ResourceLocation chest;
    private final ResourceLocation enderChest;

    private final ResourceLocation armorLeather;
    private final ResourceLocation armorCopper;
    private final ResourceLocation armorIron;
    private final ResourceLocation armorGold;
    private final ResourceLocation armorDiamond;
    private final ResourceLocation armorNetherite;

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

    private static ResourceLocation tex(String base, String name) {
        return ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, base + name + ".png");
    }

    public ResourceLocation chest(boolean ender) {
        return ender ? enderChest : chest;
    }

    public ResourceLocation saddle(ItemStack stack) {
        return stack.is(ModItems.UPGRADED_SADDLE) ? saddleUpgraded : saddle;
    }

    public ResourceLocation armor(ItemStack stack) {
        if (stack.is(Items.LEATHER_HORSE_ARMOR)) {
            return armorLeather;
        }
        if (stack.is(Items.GOLDEN_HORSE_ARMOR)) {
            return armorGold;
        }
        if (stack.is(Items.DIAMOND_HORSE_ARMOR)) {
            return armorDiamond;
        }
        return armorIron;
    }
}
