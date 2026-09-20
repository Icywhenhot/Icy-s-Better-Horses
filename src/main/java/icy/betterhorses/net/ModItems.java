package icy.betterhorses.net;

import icy.betterhorses.net.item.HorseCartItem;
import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;

public final class ModItems {

    private static <T extends Item> T item(String path, T value) {
        return Registry.register(BuiltInRegistries.ITEM,
                new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path), value);
    }

    public static final UpgradedSaddleItem UPGRADED_SADDLE = item(
            "upgraded_saddle", new UpgradedSaddleItem(new Item.Properties().stacksTo(1)));
    public static final Item HORSE_HOOVES = item(
            "horse_hooves_gear", new Item(new Item.Properties().stacksTo(1)));
    public static final Item HORSE_MEDKIT = item(
            "horse_medkit_gear", new Item(new Item.Properties().stacksTo(1)));
    public static final Item CANISTER = item(
            "canister", new Item(new Item.Properties()));
    public static final Item HORSE_STABILIZER = item(
            "horse_stabilizer_gear", new Item(new Item.Properties().stacksTo(1)));
    public static final Item HORSE_CART = item(
            "horse_cart_gear", new HorseCartItem(new Item.Properties().stacksTo(1)));
    public static final Item WHEEL = item(
            "wheel", new Item(new Item.Properties()));

    public static final SpawnEggItem ICELANDIC_HORSE_SPAWN_EGG = item("icelandic_horse_spawn_egg",
            new SpawnEggItem(ModEntities.ICELANDIC_HORSE, 0xB49A80, 0xEEE3D1, new Item.Properties()));
    public static final SpawnEggItem FRIESIAN_HORSE_SPAWN_EGG = item("friesian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.FRIESIAN_HORSE, 0x252525, 0x151515, new Item.Properties()));
    public static final SpawnEggItem HAFLINGER_HORSE_SPAWN_EGG = item("haflinger_horse_spawn_egg",
            new SpawnEggItem(ModEntities.HAFLINGER_HORSE, 0xC78A43, 0xEBD0A4, new Item.Properties()));
    public static final SpawnEggItem APPALOOSA_HORSE_SPAWN_EGG = item("appaloosa_horse_spawn_egg",
            new SpawnEggItem(ModEntities.APPALOOSA_HORSE, 0xA88A69, 0xE8E1D7, new Item.Properties()));
    public static final SpawnEggItem THOROUGHBRED_HORSE_SPAWN_EGG = item("thoroughbred_horse_spawn_egg",
            new SpawnEggItem(ModEntities.THOROUGHBRED_HORSE, 0x69452F, 0x211713, new Item.Properties()));
    public static final SpawnEggItem AMERICAN_PAINT_HORSE_SPAWN_EGG = item("american_paint_horse_spawn_egg",
            new SpawnEggItem(ModEntities.AMERICAN_PAINT_HORSE, 0xF1EDE4, 0x553A2C, new Item.Properties()));
    public static final SpawnEggItem ANDALUSIAN_HORSE_SPAWN_EGG = item("andalusian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.ANDALUSIAN_HORSE, 0xA9A9A9, 0x555555, new Item.Properties()));
    public static final SpawnEggItem MUSTANG_HORSE_SPAWN_EGG = item("mustang_horse_spawn_egg",
            new SpawnEggItem(ModEntities.MUSTANG_HORSE, 0x8B674B, 0x35261D, new Item.Properties()));
    public static final SpawnEggItem QUARTER_HORSE_SPAWN_EGG = item("quarter_horse_spawn_egg",
            new SpawnEggItem(ModEntities.QUARTER_HORSE, 0xB5783F, 0xE8C58E, new Item.Properties()));
    public static final SpawnEggItem ARABIAN_HORSE_SPAWN_EGG = item("arabian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.ARABIAN_HORSE, 0xD6D1C9, 0x77716C, new Item.Properties()));
    public static final SpawnEggItem MORGAN_HORSE_SPAWN_EGG = item("morgan_horse_spawn_egg",
            new SpawnEggItem(ModEntities.MORGAN_HORSE, 0x6B3D24, 0x21130D, new Item.Properties()));
    public static final SpawnEggItem PERCHERON_HORSE_SPAWN_EGG = item("percheron_horse_spawn_egg",
            new SpawnEggItem(ModEntities.PERCHERON_HORSE, 0xB6B6B6, 0x555555, new Item.Properties()));
    public static final SpawnEggItem SHIRE_HORSE_SPAWN_EGG = item("shire_horse_spawn_egg",
            new SpawnEggItem(ModEntities.SHIRE_HORSE, 0x3D322C, 0xE9E2D6, new Item.Properties()));
    public static final SpawnEggItem BELGIAN_HORSE_SPAWN_EGG = item("belgian_horse_spawn_egg",
            new SpawnEggItem(ModEntities.BELGIAN_HORSE, 0xB8793F, 0xE7C393, new Item.Properties()));
    public static final SpawnEggItem CLYDESDALE_HORSE_SPAWN_EGG = item("clydesdale_horse_spawn_egg",
            new SpawnEggItem(ModEntities.CLYDESDALE_HORSE, 0x6E4937, 0xF2EFE8, new Item.Properties()));

    public static final List<Item> BREED_SPAWN_EGGS = List.of(
            ICELANDIC_HORSE_SPAWN_EGG,
            FRIESIAN_HORSE_SPAWN_EGG,
            HAFLINGER_HORSE_SPAWN_EGG,
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
            CLYDESDALE_HORSE_SPAWN_EGG);

    public static final CreativeModeTab STABLE_SUPPLIES_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "stable_supplies"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> UPGRADED_SADDLE.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        BuiltInRegistries.ITEM.getOptional(new ResourceLocation(
                                        IcysBetterHorses.RESOURCE_NAMESPACE, "stable_handbook"))
                                .filter(item -> item != Items.AIR)
                                .ifPresent(output::accept);
                        output.accept(UPGRADED_SADDLE);
                        output.accept(HORSE_HOOVES);
                        output.accept(HORSE_MEDKIT);
                        output.accept(CANISTER);
                        output.accept(HORSE_STABILIZER);
                        output.accept(WHEEL);
                        output.accept(HORSE_CART);
                        BREED_SPAWN_EGGS.forEach(output::accept);
                    })
                    .build());

    public static void register() {}

    private ModItems() {}
}
