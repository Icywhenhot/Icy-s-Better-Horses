package icy.betterhorses.net.client.render;

import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
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
    private final Map<Item, Identifier> armors = new HashMap<>();
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
    private final Identifier armorGeneric;

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

    public static void register() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "tack_textures");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        for (BhTackTextures t : ALL) {
                            t.armors.clear();
                        }
                    }
                });
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
        return armors.computeIfAbsent(stack.getItem(), this::lookup);
    }

    private Identifier lookup(Item item) {
        Identifier key = BuiltInRegistries.ITEM.getKey(item);
        Identifier named = Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID,
                base + "armor/" + key.getNamespace() + "/" + key.getPath() + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(named).isPresent()) {
            return named;
        }
        if (item == Items.LEATHER_HORSE_ARMOR) {
            return armorLeather;
        }
        if (item == Items.COPPER_HORSE_ARMOR) {
            return armorCopper;
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
        if (item == Items.NETHERITE_HORSE_ARMOR) {
            return armorNetherite;
        }
        return armorGeneric;
    }
}
