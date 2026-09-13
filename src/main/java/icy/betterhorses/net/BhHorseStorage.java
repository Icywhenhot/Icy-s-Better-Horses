package icy.betterhorses.net;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BhHorseStorage {

    private BhHorseStorage() {}

    public record SlotEntry(int slot, ItemStack stack) {
        public static final Codec<SlotEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.INT.fieldOf("Slot").forGetter(SlotEntry::slot),
                        ItemStack.CODEC.fieldOf("Item").forGetter(SlotEntry::stack)
                ).apply(instance, SlotEntry::new));
    }

    public static void writeContainer(ValueOutput.TypedOutputList<SlotEntry> list, SimpleContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            list.add(new SlotEntry(i, stack));
        }
    }

    public static void readContainer(ValueInput.TypedInputList<SlotEntry> list, SimpleContainer container) {
        container.clearContent();
        for (SlotEntry entry : list) {
            int slot = entry.slot();
            if (slot < 0 || slot >= container.getContainerSize()) {
                continue;
            }
            container.setItem(slot, entry.stack());
        }
    }

    public static void restoreUpgradedSaddle(@Nullable SimpleContainer inventory, ValueInput input) {
        if (inventory == null || !inventory.getItem(0).isEmpty()) {
            return;
        }
        ItemStack saddle = input.read("SaddleItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (saddle.is(ModItems.UPGRADED_SADDLE)) {
            inventory.setItem(0, saddle);
        }
    }

    public static @Nullable BlockPos readLegacyBlockPos(ValueInput input, String keyPrefix) {
        Optional<Integer> x = input.getInt(keyPrefix + "X");
        Optional<Integer> y = input.getInt(keyPrefix + "Y");
        Optional<Integer> z = input.getInt(keyPrefix + "Z");
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return null;
        }

        return new BlockPos(x.get(), y.get(), z.get());
    }

    public static void dropContainerContents(AbstractHorse horse, ServerLevel level, SimpleContainer container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                horse.spawnAtLocation(level, stack);
            }
        }
    }
}
