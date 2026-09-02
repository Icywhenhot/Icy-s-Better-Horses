package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class SlowBlockImmunity implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
    }

    public static boolean ignoresSlowBlocks(IHorseData data) {
        return switch (data.bh_getBreed()) {
            case PERCHERON, ICELANDIC -> true;
            default -> false;
        };
    }
}
