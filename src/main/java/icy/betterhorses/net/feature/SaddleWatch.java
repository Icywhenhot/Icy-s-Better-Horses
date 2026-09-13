package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;

public final class SaddleWatch implements HorseFeature {

    private boolean hadUpgradedSaddle;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide() || horse.isRemoved()) {
            return;
        }
        boolean hasUpgradedSaddle = data.bh_hasUpgradedSaddle();
        if (this.hadUpgradedSaddle && !hasUpgradedSaddle) {
            data.bh_onUpgradedSaddleRemoved(ItemStack.EMPTY);
        }
        this.hadUpgradedSaddle = hasUpgradedSaddle;
    }

    @Override
    public void onLoad(AbstractHorse horse, IHorseData data) {
        this.hadUpgradedSaddle = data.bh_hasUpgradedSaddle();
    }

    @Override
    public void onInventoryChanged(AbstractHorse horse, IHorseData data) {
        this.hadUpgradedSaddle = data.bh_hasUpgradedSaddle();
    }
}
