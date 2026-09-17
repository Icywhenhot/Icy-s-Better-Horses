package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BhHorseStorage {

    private BhHorseStorage() {}

    public static void writeContainer(CompoundTag tag, String key, SimpleContainer container) {
        ListTag list = new ListTag();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putByte("Slot", (byte) i);
            list.add(stack.save(entry));
        }
        tag.put(key, list);
    }

    public static void readContainer(CompoundTag tag, String key, SimpleContainer container) {
        container.clearContent();
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < 0 || slot >= container.getContainerSize()) {
                continue;
            }
            container.setItem(slot, ItemStack.of(entry));
        }
    }

    public static void restoreUpgradedSaddle(@Nullable SimpleContainer inventory, CompoundTag tag) {
        if (inventory == null || !inventory.getItem(0).isEmpty()) {
            return;
        }
        ItemStack saddle = tag.contains("SaddleItem", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("SaddleItem"))
                : ItemStack.EMPTY;
        if (saddle.is(ModItems.UPGRADED_SADDLE.get())) {
            inventory.setItem(0, saddle);
        }
    }

    public static @Nullable BlockPos readLegacyBlockPos(CompoundTag tag, String keyPrefix) {
        if (!tag.contains(keyPrefix + "X", Tag.TAG_INT)
                || !tag.contains(keyPrefix + "Y", Tag.TAG_INT)
                || !tag.contains(keyPrefix + "Z", Tag.TAG_INT)) {
            return null;
        }
        return new BlockPos(tag.getInt(keyPrefix + "X"), tag.getInt(keyPrefix + "Y"), tag.getInt(keyPrefix + "Z"));
    }

    public static boolean contains(CompoundTag tag, String key, ItemStack wanted) {
        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (ItemStack.of(list.getCompound(i)).is(wanted.getItem())) return true;
        }
        return false;
    }

    public static void dropContainerContents(AbstractHorse horse, ServerLevel level, SimpleContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                horse.spawnAtLocation(stack);
            }
        }
    }
}
