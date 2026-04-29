package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;

public final class ModItems {

    public static final Item UPGRADED_SADDLE = register("upgraded_saddle",
            UpgradedSaddleItem::new,
            new Item.Properties().stacksTo(1).equippable(EquipmentSlot.SADDLE));

    public static final Item HORSE_HOOVES = register("horse_hooves_gear",
            Item::new,
            new Item.Properties().stacksTo(1));

    // Keep the original medkit id so existing worlds keep their saved item stacks.
    public static final Item HORSE_MEDKIT = register("horse_medkit_gear",
            Item::new,
            new Item.Properties().stacksTo(1));

    public static final Item CANISTER = register("canister",
            Item::new,
            new Item.Properties());

    public static final Item HITCHPOST = register("hitchpost",
            properties -> new BlockItem(ModBlocks.HITCHPOST, properties),
            new Item.Properties().stacksTo(16));

    public static final Item HORSE_STABILIZER = register("horse_stabilizer_gear",
            Item::new,
            new Item.Properties().stacksTo(1));

    public static final CreativeModeTab STABLE_SUPPLIES_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_supplies"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> new ItemStack(UPGRADED_SADDLE))
                    .displayItems((parameters, entries) -> {
                        Item handbook = BuiltInRegistries.ITEM.getValue(
                                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_handbook"));
                        if (handbook != Items.AIR) {
                            entries.accept(handbook);
                        }
                        entries.accept(UPGRADED_SADDLE);
                        entries.accept(HORSE_HOOVES);
                        entries.accept(HORSE_MEDKIT);
                        entries.accept(CANISTER);
                        entries.accept(HORSE_STABILIZER);
                        entries.accept(HITCHPOST);
                    })
                    .build());

    public static void init() {
    }

    private static Item register(String path, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    private ModItems() {}
}
