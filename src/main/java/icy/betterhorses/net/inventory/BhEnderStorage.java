package icy.betterhorses.net.inventory;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class BhEnderStorage {

    private static final boolean LOADED = ModList.get().isLoaded("enderstorage");
    private static final ResourceLocation CHEST = new ResourceLocation("enderstorage", "ender_chest");

    private BhEnderStorage() {}

    public static boolean isChest(ItemStack stack) {
        return LOADED && !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(CHEST);
    }

    public static Container open(ItemStack stack, Player player) {
        return Link.open(stack, player);
    }

    private static final class Link {
        static Container open(ItemStack stack, Player player) {
            Frequency freq = Frequency.readFromStack(stack);
            if (freq.hasOwner() && !freq.getOwner().equals(player.getUUID())) {
                return new SimpleContainer(0);
            }
            return EnderStorageManager.instance(false).getStorage(freq, EnderItemStorage.TYPE);
        }
    }
}
