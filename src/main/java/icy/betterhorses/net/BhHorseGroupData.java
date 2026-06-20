package icy.betterhorses.net;

import net.minecraft.world.entity.SpawnGroupData;

// Shared breed for siblings spawned in one natural-spawn group; wraps vanilla's group data.
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
