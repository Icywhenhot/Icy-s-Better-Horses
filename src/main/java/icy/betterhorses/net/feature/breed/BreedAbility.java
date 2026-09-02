package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public interface BreedAbility {

    void tick(AbstractHorse horse, IHorseData data, BhAbilityState state);

    default void onDetach(AbstractHorse horse, IHorseData data) {}
}
