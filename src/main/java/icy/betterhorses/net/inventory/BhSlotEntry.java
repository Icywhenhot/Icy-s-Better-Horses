package icy.betterhorses.net.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record BhSlotEntry(int slot, ItemStack stack) {
    public static final Codec<BhSlotEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("Slot").forGetter(BhSlotEntry::slot),
            ItemStack.CODEC.fieldOf("Item").forGetter(BhSlotEntry::stack)
    ).apply(instance, BhSlotEntry::new));
}
