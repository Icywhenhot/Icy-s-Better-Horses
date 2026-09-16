package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import icy.betterhorses.net.item.HorseCartItem;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {

    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    public static final Item UPGRADED_SADDLE = register("upgraded_saddle",
            new UpgradedSaddleItem(itemProperties("upgraded_saddle")
                    .stacksTo(1)));

    public static final Item HORSE_HOOVES = register("horse_hooves_gear",
            new Item(itemProperties("horse_hooves_gear").stacksTo(1)));

    public static final Item HORSE_MEDKIT = register("horse_medkit_gear",
            new Item(itemProperties("horse_medkit_gear").stacksTo(1)));

    public static final Item CANISTER = register("canister",
            new Item(itemProperties("canister")));

    public static final Item HORSE_STABILIZER = register("horse_stabilizer_gear",
            new Item(itemProperties("horse_stabilizer_gear").stacksTo(1)));

    public static final Item HORSE_CART = register("horse_cart_gear",
            new HorseCartItem(itemProperties("horse_cart_gear").stacksTo(1)));

    public static final Item WHEEL = register("wheel",
            new Item(itemProperties("wheel")));

    public static final Item ICELANDIC_HORSE_SPAWN_EGG = register("icelandic_horse_spawn_egg",
            new SpawnEggItem(ModEntities.ICELANDIC_HORSE, 0xB49A80, 0xEEE3D1,
                    itemProperties("icelandic_horse_spawn_egg")));

    public static final Item FRIESIAN_HORSE_SPAWN_EGG = register("friesian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.FRIESIAN_HORSE, 0x252525, 0x151515,
                    itemProperties("friesian_horse_spawn_egg")));

    public static final Item HAFLINGER_HORSE_SPAWN_EGG = register("haflinger_horse_spawn_egg",
            new SpawnEggItem(ModEntities.HAFLINGER_HORSE, 0xC78A43, 0xEBD0A4,
                    itemProperties("haflinger_horse_spawn_egg")));

    public static final Item APPALOOSA_HORSE_SPAWN_EGG = register("appaloosa_horse_spawn_egg",
            new SpawnEggItem(ModEntities.APPALOOSA_HORSE, 0xA88A69, 0xE8E1D7,
                    itemProperties("appaloosa_horse_spawn_egg")));

    public static final Item THOROUGHBRED_HORSE_SPAWN_EGG = register("thoroughbred_horse_spawn_egg",
            new SpawnEggItem(ModEntities.THOROUGHBRED_HORSE, 0x69452F, 0x211713,
                    itemProperties("thoroughbred_horse_spawn_egg")));

    public static final Item AMERICAN_PAINT_HORSE_SPAWN_EGG = register("american_paint_horse_spawn_egg",
            new SpawnEggItem(ModEntities.AMERICAN_PAINT_HORSE, 0xF1EDE4, 0x553A2C,
                    itemProperties("american_paint_horse_spawn_egg")));

    public static final Item ANDALUSIAN_HORSE_SPAWN_EGG = register("andalusian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.ANDALUSIAN_HORSE, 0xA9A9A9, 0x555555,
                    itemProperties("andalusian_horse_spawn_egg")));

    public static final Item MUSTANG_HORSE_SPAWN_EGG = register("mustang_horse_spawn_egg",
            new SpawnEggItem(ModEntities.MUSTANG_HORSE, 0x8B674B, 0x35261D,
                    itemProperties("mustang_horse_spawn_egg")));

    public static final Item QUARTER_HORSE_SPAWN_EGG = register("quarter_horse_spawn_egg",
            new SpawnEggItem(ModEntities.QUARTER_HORSE, 0xB5783F, 0xE8C58E,
                    itemProperties("quarter_horse_spawn_egg")));

    public static final Item ARABIAN_HORSE_SPAWN_EGG = register("arabian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.ARABIAN_HORSE, 0xD6D1C9, 0x77716C,
                    itemProperties("arabian_horse_spawn_egg")));

    public static final Item MORGAN_HORSE_SPAWN_EGG = register("morgan_horse_spawn_egg",
            new SpawnEggItem(ModEntities.MORGAN_HORSE, 0x6B3D24, 0x21130D,
                    itemProperties("morgan_horse_spawn_egg")));

    public static final Item PERCHERON_HORSE_SPAWN_EGG = register("percheron_horse_spawn_egg",
            new SpawnEggItem(ModEntities.PERCHERON_HORSE, 0xB6B6B6, 0x555555,
                    itemProperties("percheron_horse_spawn_egg")));

    public static final Item SHIRE_HORSE_SPAWN_EGG = register("shire_horse_spawn_egg",
            new SpawnEggItem(ModEntities.SHIRE_HORSE, 0x3D322C, 0xE9E2D6,
                    itemProperties("shire_horse_spawn_egg")));

    public static final Item BELGIAN_HORSE_SPAWN_EGG = register("belgian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.BELGIAN_HORSE, 0xB8793F, 0xE7C393,
                    itemProperties("belgian_horse_spawn_egg")));

    public static final Item CLYDESDALE_HORSE_SPAWN_EGG = register("clydesdale_horse_spawn_egg",
            new SpawnEggItem(ModEntities.CLYDESDALE_HORSE, 0x6E4937, 0xF2EFE8,
                    itemProperties("clydesdale_horse_spawn_egg")));

    public static final List<Item> BREED_SPAWN_EGGS = List.of(
            ICELANDIC_HORSE_SPAWN_EGG,
            FRIESIAN_HORSE_SPAWN_EGG,
            APPALOOSA_HORSE_SPAWN_EGG,
            THOROUGHBRED_HORSE_SPAWN_EGG,
            AMERICAN_PAINT_HORSE_SPAWN_EGG,
            ANDALUSIAN_HORSE_SPAWN_EGG,
            MUSTANG_HORSE_SPAWN_EGG,
            QUARTER_HORSE_SPAWN_EGG,
            ARABIAN_HORSE_SPAWN_EGG,
            MORGAN_HORSE_SPAWN_EGG,
            PERCHERON_HORSE_SPAWN_EGG,
            SHIRE_HORSE_SPAWN_EGG,
            BELGIAN_HORSE_SPAWN_EGG,
            CLYDESDALE_HORSE_SPAWN_EGG,
            HAFLINGER_HORSE_SPAWN_EGG);

    public static final CreativeModeTab STABLE_SUPPLIES_TAB = CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> new ItemStack(UPGRADED_SADDLE))
                    .displayItems((parameters, entries) -> {
                        entries.accept(UPGRADED_SADDLE);
                        entries.accept(HORSE_HOOVES);
                        entries.accept(HORSE_MEDKIT);
                        entries.accept(CANISTER);
                        entries.accept(HORSE_STABILIZER);
                        entries.accept(WHEEL);
                        entries.accept(HORSE_CART);
                        BREED_SPAWN_EGGS.forEach(entries::accept);
                    })
                    .build();

    public static void register(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> ITEMS.forEach((path, item) ->
                helper.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path), item)));
        event.register(
                Registries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "stable_supplies"),
                () -> STABLE_SUPPLIES_TAB);
    }

    private static Item register(String path, Item item) {
        ITEMS.put(path, item);
        return item;
    }

    private static Item.Properties itemProperties(String path) {
        return new Item.Properties();
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, path));
    }

    private ModItems() {}
}
