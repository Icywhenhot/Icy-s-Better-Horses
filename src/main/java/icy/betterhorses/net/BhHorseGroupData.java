package icy.betterhorses.net;

import net.minecraft.world.entity.SpawnGroupData;

public final class BhHorseGroupData implements SpawnGroupData {
    private final HorseBreed breed;
    @SuppressWarnings("unused")
    private final SpawnGroupData wrapped;

    public BhHorseGroupData(HorseBreed breed, SpawnGroupData wrapped) {
        this.breed = breed;
        this.wrapped = wrapped;
    }

    public HorseBreed breed() {
        return breed;
    }
}
