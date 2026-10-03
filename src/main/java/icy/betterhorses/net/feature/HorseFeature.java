package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public interface HorseFeature {

    void tick(AbstractHorse horse, IHorseData data);

    default void onLoad(AbstractHorse horse, IHorseData data) {}

    default void onRemoved(AbstractHorse horse, IHorseData data) {}

    default void onInventoryChanged(AbstractHorse horse, IHorseData data) {}
}
