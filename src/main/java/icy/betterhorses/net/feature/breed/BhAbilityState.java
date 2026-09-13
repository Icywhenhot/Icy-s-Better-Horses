package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class BhAbilityState {

    private static final double MOVING = 0.03D;

    public final BhBreedAbilities.MovementSampler movement = new BhBreedAbilities.MovementSampler();

    private int standstill;
    private int moving;

    public void tick(AbstractHorse horse) {
        movement.sample(horse);
        if (movement.speed() > MOVING) {
            moving++;
            standstill = 0;
        } else {
            standstill++;
            moving = 0;
        }
    }

    public int standstillTicks() {
        return standstill;
    }

    public int movingTicks() {
        return moving;
    }

    public boolean gallopingFlat(AbstractHorse horse, IHorseData data) {
        int gear = data.bh_getGear();
        return (gear == 0 || gear == BhGears.TOP_GEAR) && movement.isRunningFlat();
    }
}
