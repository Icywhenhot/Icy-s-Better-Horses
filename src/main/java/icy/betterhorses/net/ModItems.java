package icy.betterhorses.net;

import icy.betterhorses.net.item.UpgradedSaddleItem;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(IcysBetterHorses.registryOwnerId(), Registries.ITEM);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(IcysBetterHorses.registryOwnerId(), Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<Item> UPGRADED_SADDLE = ITEMS.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "upgraded_saddle"),
            () -> new UpgradedSaddleItem(withId("upgraded_saddle", new Item.Properties().stacksTo(1).equippable(EquipmentSlot.SADDLE))));
    public static final RegistrySupplier<Item> HORSE_HOOVES = ITEMS.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_hooves_gear"),
            () -> new Item(withId("horse_hooves_gear", new Item.Properties().stacksTo(1))));
    public static final RegistrySupplier<Item> HORSE_MEDKIT = ITEMS.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_medkit_gear"),
            () -> new Item(withId("horse_medkit_gear", new Item.Properties().stacksTo(1))));
    public static final RegistrySupplier<Item> CANISTER = ITEMS.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "canister"),
            () -> new Item(withId("canister", new Item.Properties())));
    public static final RegistrySupplier<Item> HITCHPOST = ITEMS.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"),
            () -> new BlockItem(ModBlocks.HITCHPOST.get(), withId("hitchpost", new Item.Properties().stacksTo(16))));
    public static final RegistrySupplier<Item> HORSE_STABILIZER = ITEMS.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "horse_stabilizer_gear"),
            () -> new Item(withId("horse_stabilizer_gear", new Item.Properties().stacksTo(1))));

    public static final RegistrySupplier<CreativeModeTab> STABLE_SUPPLIES_TAB = TABS.register(
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_supplies"),
            () ->
            CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable("itemGroup.icys-better-horses.stable_supplies"))
                    .icon(() -> new ItemStack(UPGRADED_SADDLE.get()))
                    .displayItems((parameters, entries) -> {
                        Item handbook = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "stable_handbook"));
                        if (handbook != Items.AIR) entries.accept(handbook);
                        entries.accept(UPGRADED_SADDLE.get());
                        entries.accept(HORSE_HOOVES.get());
                        entries.accept(HORSE_MEDKIT.get());
                        entries.accept(CANISTER.get());
                        entries.accept(HORSE_STABILIZER.get());
                        entries.accept(HITCHPOST.get());
                    })));

    public static void init() {
        ITEMS.register();
        if (!dev.architectury.platform.Platform.isNeoForge()) {
            TABS.register();
        }
    }

    private static Item.Properties withId(String path, Item.Properties properties) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return properties.setId(key);
    }

    private ModItems() {}
}
