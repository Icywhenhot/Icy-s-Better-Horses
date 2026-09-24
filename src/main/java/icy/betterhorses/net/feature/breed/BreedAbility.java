package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.nbt.CompoundTag;

public interface BreedAbility {

    void tick(AbstractHorse horse, IHorseData data, BhAbilityState state);

    default void onDetach(AbstractHorse horse, IHorseData data) {}

    default boolean hasActiveSkill() {
        return false;
    }

    default void onActivate(AbstractHorse horse, IHorseData data) {}

    default void save(CompoundTag tag) {}

    default void load(CompoundTag tag) {}
}
