package icy.betterhorses.net;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;

public final class BhBanners {

    private BhBanners() {}

    public static boolean fits(ItemStack stack) {
        return stack.getItem() instanceof BannerItem || heralds(stack);
    }

    public static boolean heralds(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("heralds_luna") && id.getPath().startsWith("banner_");
    }

    public static ItemStack renderCopy(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = new ItemStack(stack.getItem());
        CompoundTag tag = stack.getTag();
        if (tag == null) return copy;
        if (tag.contains("CustomModelData", Tag.TAG_INT)) {
            copy.getOrCreateTag().putInt("CustomModelData", tag.getInt("CustomModelData"));
        }
        CompoundTag block = tag.getCompound("BlockEntityTag");
        if (block.contains("Patterns", Tag.TAG_LIST)) {
            var patterns = block.getList("Patterns", Tag.TAG_COMPOUND);
            var clean = new ListTag();
            for (int i = 0; i < patterns.size() && clean.size() < 16; i++) {
                CompoundTag pattern = patterns.getCompound(i);
                String id = pattern.getString("Pattern");
                if (BannerPattern.byHash(id) == null) continue;
                CompoundTag layer = new CompoundTag();
                layer.putString("Pattern", id);
                layer.putInt("Color", pattern.getInt("Color"));
                clean.add(layer);
            }
            copy.getOrCreateTagElement("BlockEntityTag").put("Patterns", clean);
        }
        if (block.contains("Base", Tag.TAG_INT)) {
            copy.getOrCreateTagElement("BlockEntityTag").putInt("Base", block.getInt("Base"));
        }
        CompoundTag display = tag.getCompound("display");
        if (display.contains("color", Tag.TAG_INT)) {
            copy.getOrCreateTagElement("display").putInt("color", display.getInt("color"));
        }
        return copy;
    }
}
