package icy.betterhorses.net;

import net.minecraft.world.entity.SpawnGroupData;

/**
 * Spawn group data carried between sibling horses spawned in the same natural-spawn group.
 * All horses sharing this data wear the same breed.
 *
 * <p>{@code wrapped} preserves any vanilla {@link SpawnGroupData} produced by vanilla's
 * {@code Horse.finalizeSpawn} (e.g. its own variant-sharing group data) so it isn't lost.
 */
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
