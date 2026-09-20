package icy.betterhorses.net;

import icy.betterhorses.net.item.HorseCartItem;
import icy.betterhorses.net.item.UpgradedSaddleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IcysBetterHorses.RESOURCE_NAMESPACE);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IcysBetterHorses.RESOURCE_NAMESPACE);

    public static final RegistryObject<UpgradedSaddleItem> UPGRADED_SADDLE = ITEMS.register(
            "upgraded_saddle",
            () -> new UpgradedSaddleItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HORSE_HOOVES = ITEMS.register(
            "horse_hooves_gear",
            () -> new Item(new Item.Properties().stacksTo(1)) {
                @Override
                public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
                    return enchantment == Enchantments.FROST_WALKER;
                }
            });
    public static final RegistryObject<Item> HORSE_MEDKIT = ITEMS.register(
            "horse_medkit_gear",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CANISTER = ITEMS.register(
            "canister",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HORSE_STABILIZER = ITEMS.register(
            "horse_stabilizer_gear",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HORSE_CART = ITEMS.register(
            "horse_cart_gear",
            () -> new HorseCartItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WHEEL = ITEMS.register(
            "wheel",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ICELANDIC_HORSE_SPAWN_EGG = ITEMS.register(
            "icelandic_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ICELANDIC_HORSE, 0xB49A80, 0xEEE3D1, new Item.Properties()));
    public static final RegistryObject<Item> FRIESIAN_HORSE_SPAWN_EGG = ITEMS.register(
            "friesian_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.FRIESIAN_HORSE, 0x252525, 0x151515, new Item.Properties()));
    public static final RegistryObject<Item> HAFLINGER_HORSE_SPAWN_EGG = ITEMS.register(
            "haflinger_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.HAFLINGER_HORSE, 0xC78A43, 0xEBD0A4, new Item.Properties()));
    public static final RegistryObject<Item> APPALOOSA_HORSE_SPAWN_EGG = ITEMS.register(
            "appaloosa_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.APPALOOSA_HORSE, 0xA88A69, 0xE8E1D7, new Item.Properties()));
    public static final RegistryObject<Item> THOROUGHBRED_HORSE_SPAWN_EGG = ITEMS.register(
            "thoroughbred_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.THOROUGHBRED_HORSE, 0x69452F, 0x211713, new Item.Properties()));
    public static final RegistryObject<Item> AMERICAN_PAINT_HORSE_SPAWN_EGG = ITEMS.register(
            "american_paint_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.AMERICAN_PAINT_HORSE, 0xF1EDE4, 0x553A2C, new Item.Properties()));
    public static final RegistryObject<Item> ANDALUSIAN_HORSE_SPAWN_EGG = ITEMS.register(
            "andalusian_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ANDALUSIAN_HORSE, 0xA9A9A9, 0x555555, new Item.Properties()));
    public static final RegistryObject<Item> MUSTANG_HORSE_SPAWN_EGG = ITEMS.register(
            "mustang_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MUSTANG_HORSE, 0x8B674B, 0x35261D, new Item.Properties()));
    public static final RegistryObject<Item> QUARTER_HORSE_SPAWN_EGG = ITEMS.register(
            "quarter_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.QUARTER_HORSE, 0xB5783F, 0xE8C58E, new Item.Properties()));
    public static final RegistryObject<Item> ARABIAN_HORSE_SPAWN_EGG = ITEMS.register(
            "arabian_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ARABIAN_HORSE, 0xD6D1C9, 0x77716C, new Item.Properties()));
    public static final RegistryObject<Item> MORGAN_HORSE_SPAWN_EGG = ITEMS.register(
            "morgan_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.MORGAN_HORSE, 0x6B3D24, 0x21130D, new Item.Properties()));
    public static final RegistryObject<Item> PERCHERON_HORSE_SPAWN_EGG = ITEMS.register(
            "percheron_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.PERCHERON_HORSE, 0xB6B6B6, 0x555555, new Item.Properties()));
    public static final RegistryObject<Item> SHIRE_HORSE_SPAWN_EGG = ITEMS.register(
            "shire_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.SHIRE_HORSE, 0x3D322C, 0xE9E2D6, new Item.Properties()));
    public static final RegistryObject<Item> BELGIAN_HORSE_SPAWN_EGG = ITEMS.register(
            "belgian_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BELGIAN_HORSE, 0xB8793F, 0xE7C393, new Item.Properties()));
    public static final RegistryObject<Item> CLYDESDALE_HORSE_SPAWN_EGG = ITEMS.register(
            "clydesdale_horse_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.CLYDESDALE_HORSE, 0x6E4937, 0xF2EFE8, new Item.Properties()));

    public static final List<RegistryObject<Item>> BREED_SPAWN_EGGS = List.of(
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

    public static final RegistryObject<CreativeModeTab> STABLE_SUPPLIES_TAB = TABS.register("stable_supplies", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> UPGRADED_SADDLE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "stable_handbook"))
                                .filter(item -> item != Items.AIR)
                                .ifPresent(output::accept);
                        output.accept(UPGRADED_SADDLE.get());
                        output.accept(HORSE_HOOVES.get());
                        output.accept(HORSE_MEDKIT.get());
                        output.accept(CANISTER.get());
                        output.accept(HORSE_STABILIZER.get());
                        output.accept(WHEEL.get());
                        output.accept(HORSE_CART.get());
                        BREED_SPAWN_EGGS.forEach(egg -> output.accept(egg.get()));
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
    }

    private ModItems() {}
}
