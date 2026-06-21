package icy.betterhorses.net;

import net.minecraft.world.entity.SpawnGroupData;

// Spawn group data carried between sibling horses in the same natural-spawn group; all share one breed. wrapped preserves any vanilla SpawnGroupData so it isn't lost.
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
