package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.equipment.Equippable;
import icy.betterhorses.net.item.HorseCartItem;
import java.util.List;

public final class ModItems {

    public static final Item UPGRADED_SADDLE = register("upgraded_saddle",
            new UpgradedSaddleItem(itemProperties("upgraded_saddle")
                    .stacksTo(1)
                    .component(DataComponents.EQUIPPABLE, Equippable.saddle())));

    public static final Item HORSE_HOOVES = register("horse_hooves_gear",
            new Item(itemProperties("horse_hooves_gear").stacksTo(1).enchantable(15)));

    public static final Item HORSE_MEDKIT = register("horse_medkit_gear",
            new Item(itemProperties("horse_medkit_gear").stacksTo(1)));

    public static final Item CANISTER = register("canister",
            new Item(itemProperties("canister")));

    public static final Item HITCHPOST = register("hitchpost",
            new BlockItem(ModBlocks.HITCHPOST, blockItemProperties("hitchpost").stacksTo(16)));

    public static final Item HORSE_STABILIZER = register("horse_stabilizer_gear",
            new Item(itemProperties("horse_stabilizer_gear").stacksTo(1)));

    public static final Item HORSE_CART = register("horse_cart_gear",
            new HorseCartItem(itemProperties("horse_cart_gear").stacksTo(1)));

    public static final Item WHEEL = register("wheel",
            new Item(itemProperties("wheel")));

    public static final Item ICELANDIC_HORSE_SPAWN_EGG = register("icelandic_horse_spawn_egg",
            new SpawnEggItem(itemProperties("icelandic_horse_spawn_egg")
                    .spawnEgg(ModEntities.ICELANDIC_HORSE)));

    public static final Item FRIESIAN_HORSE_SPAWN_EGG = register("friesian_horse_spawn_egg",
            new SpawnEggItem(itemProperties("friesian_horse_spawn_egg")
                    .spawnEgg(ModEntities.FRIESIAN_HORSE)));

    public static final Item HAFLINGER_HORSE_SPAWN_EGG = register("haflinger_horse_spawn_egg",
            new SpawnEggItem(itemProperties("haflinger_horse_spawn_egg")
                    .spawnEgg(ModEntities.HAFLINGER_HORSE)));

    public static final Item APPALOOSA_HORSE_SPAWN_EGG = register("appaloosa_horse_spawn_egg",
            new SpawnEggItem(itemProperties("appaloosa_horse_spawn_egg")
                    .spawnEgg(ModEntities.APPALOOSA_HORSE)));

    public static final Item THOROUGHBRED_HORSE_SPAWN_EGG = register("thoroughbred_horse_spawn_egg",
            new SpawnEggItem(itemProperties("thoroughbred_horse_spawn_egg")
                    .spawnEgg(ModEntities.THOROUGHBRED_HORSE)));

    public static final Item AMERICAN_PAINT_HORSE_SPAWN_EGG = register("american_paint_horse_spawn_egg",
            new SpawnEggItem(itemProperties("american_paint_horse_spawn_egg")
                    .spawnEgg(ModEntities.AMERICAN_PAINT_HORSE)));

    public static final Item ANDALUSIAN_HORSE_SPAWN_EGG = register("andalusian_horse_spawn_egg",
            new SpawnEggItem(itemProperties("andalusian_horse_spawn_egg")
                    .spawnEgg(ModEntities.ANDALUSIAN_HORSE)));

    public static final Item MUSTANG_HORSE_SPAWN_EGG = register("mustang_horse_spawn_egg",
            new SpawnEggItem(itemProperties("mustang_horse_spawn_egg")
                    .spawnEgg(ModEntities.MUSTANG_HORSE)));

    public static final Item QUARTER_HORSE_SPAWN_EGG = register("quarter_horse_spawn_egg",
            new SpawnEggItem(itemProperties("quarter_horse_spawn_egg")
                    .spawnEgg(ModEntities.QUARTER_HORSE)));

    public static final Item ARABIAN_HORSE_SPAWN_EGG = register("arabian_horse_spawn_egg",
            new SpawnEggItem(itemProperties("arabian_horse_spawn_egg")
                    .spawnEgg(ModEntities.ARABIAN_HORSE)));

    public static final Item MORGAN_HORSE_SPAWN_EGG = register("morgan_horse_spawn_egg",
            new SpawnEggItem(itemProperties("morgan_horse_spawn_egg")
                    .spawnEgg(ModEntities.MORGAN_HORSE)));

    public static final Item PERCHERON_HORSE_SPAWN_EGG = register("percheron_horse_spawn_egg",
            new SpawnEggItem(itemProperties("percheron_horse_spawn_egg")
                    .spawnEgg(ModEntities.PERCHERON_HORSE)));

    public static final Item SHIRE_HORSE_SPAWN_EGG = register("shire_horse_spawn_egg",
            new SpawnEggItem(itemProperties("shire_horse_spawn_egg")
                    .spawnEgg(ModEntities.SHIRE_HORSE)));

    public static final Item BELGIAN_HORSE_SPAWN_EGG = register("belgian_horse_spawn_egg",
            new SpawnEggItem(itemProperties("belgian_horse_spawn_egg")
                    .spawnEgg(ModEntities.BELGIAN_HORSE)));

    public static final Item CLYDESDALE_HORSE_SPAWN_EGG = register("clydesdale_horse_spawn_egg",
            new SpawnEggItem(itemProperties("clydesdale_horse_spawn_egg")
                    .spawnEgg(ModEntities.CLYDESDALE_HORSE)));

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

    public static final CreativeModeTab STABLE_SUPPLIES_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_supplies"),
            FabricCreativeModeTab.builder()
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
                        entries.accept(HITCHPOST);
                        BREED_SPAWN_EGGS.forEach(entries::accept);
                    })
                    .build());

    public static void init() {
    }

    private static Item register(String path, Item item) {
        return Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                item);
    }

    private static Item.Properties itemProperties(String path) {
        return new Item.Properties().setId(itemKey(path));
    }

    private static Item.Properties blockItemProperties(String path) {
        return itemProperties(path).useBlockDescriptionPrefix();
    }

    private static ResourceKey<Item> itemKey(String path) {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path));
    }

    private ModItems() {}
}
