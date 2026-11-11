package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.mixin.ItemEntityAccessor;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

public final class DonkeyForaging {

    private DonkeyForaging() {}

    public static boolean pickup(AbstractHorse horse, IHorseData data, Predicate<ItemStack> accepts) {
        if (!data.bh_hasChestGear() || data.bh_hasEnderChestGear() || horse.isBaby()) {
            return false;
        }
        boolean picked = false;
        for (ItemEntity item : horse.level().getEntitiesOfClass(ItemEntity.class,
                horse.getBoundingBox().inflate(6.0D), item -> item.isAlive() && !item.hasPickUpDelay()
                        && horse.distanceToSqr(item) <= 36.0D
                        && (((ItemEntityAccessor) item).bh_getTarget() == null
                            || Objects.equals(((ItemEntityAccessor) item).bh_getTarget(), data.bh_getOwner()))
                        && accepts.test(item.getItem()))) {
            ItemStack stack = item.getItem();
            ItemStack rest = insert(data, stack);
            if (rest.getCount() == stack.getCount()) {
                continue;
            }
            picked = true;
            if (rest.isEmpty()) {
                item.discard();
            } else {
                item.setItem(rest);
            }
        }
        return picked;
    }

    public static ItemStack insert(IHorseData data, ItemStack stack) {
        var chest = data.bh_getChestContainer();
        int size = Math.min(chest.getContainerSize(), data.bh_getChestRows() * 9);
        if (size <= 0) {
            return stack;
        }
        ItemStack rest = stack.copy();
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < size && !rest.isEmpty(); i++) {
                ItemStack stored = chest.getItem(i);
                if (pass == 0 && !stored.isEmpty() && ItemStack.isSameItemSameComponents(stored, rest)) {
                    int count = Math.min(rest.getCount(), Math.max(0, chest.getMaxStackSize(rest) - stored.getCount()));
                    stored.grow(count);
                    rest.shrink(count);
                    if (count > 0) chest.setChanged();
                } else if (pass == 1 && stored.isEmpty()) {
                    chest.setItem(i, rest.split(chest.getMaxStackSize(rest)));
                }
            }
        }
        return rest;
    }
}
