package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public final class BhTackTextures {

    public static final BhTackTextures BELGIAN = new BhTackTextures("belgian");
    public static final BhTackTextures FRIESIAN = new BhTackTextures("friesian");
    public static final BhTackTextures HAFLINGER = new BhTackTextures("haflinger");
    public static final BhTackTextures ICELANDIC = new BhTackTextures("icelandic");
    public static final BhTackTextures MEDIUM = new BhTackTextures("medium");
    public static final BhTackTextures PERCHERON = new BhTackTextures("percheron");
    public static final BhTackTextures SHIRE = new BhTackTextures("shire");
    public static final BhTackTextures SMALL = new BhTackTextures("small");

    private static final BhTackTextures[] ALL = {BELGIAN, FRIESIAN, HAFLINGER, ICELANDIC, MEDIUM, PERCHERON, SHIRE, SMALL};

    private final String base;
    private final Map<Item, ResourceLocation> armors = new HashMap<>();

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
    private final ResourceLocation armorGeneric;

    private BhTackTextures(String breed) {
        this.base = "textures/entity/horse/" + breed + "/";
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
        this.armorGeneric = tex(base, "armor_generic");
    }

    public static void clearCache() {
        for (BhTackTextures t : ALL) {
            t.armors.clear();
        }
    }

    private static ResourceLocation tex(String base, String name) {
        return new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, base + name + ".png");
    }

    public ResourceLocation chest(boolean ender) {
        return ender ? enderChest : chest;
    }

    public ResourceLocation saddle(boolean upgraded) {
        return upgraded ? saddleUpgraded : saddle;
    }

    public ResourceLocation armor(ItemStack stack) {
        return armors.computeIfAbsent(stack.getItem(), this::lookup);
    }

    private ResourceLocation lookup(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        ResourceLocation named = new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE,
                base + "armor/" + key.getNamespace() + "/" + key.getPath() + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(named).isPresent()) {
            return named;
        }
        if (item == Items.LEATHER_HORSE_ARMOR) {
            return armorLeather;
        }
        if (item == Items.IRON_HORSE_ARMOR) {
            return armorIron;
        }
        if (item == Items.GOLDEN_HORSE_ARMOR) {
            return armorGold;
        }
        if (item == Items.DIAMOND_HORSE_ARMOR) {
            return armorDiamond;
        }
        return armorGeneric;
    }
}
