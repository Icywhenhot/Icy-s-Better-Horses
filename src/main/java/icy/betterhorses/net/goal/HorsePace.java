package icy.betterhorses.net.goal;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

final class HorsePace {

    private HorsePace() {
    }

    // AI ground speed grows with the square of (modifier * movement speed), a rider's with movement
    // speed alone, so this is the navigation modifier for a share of the horse's own ridden gallop.
    static double modifier(AbstractHorse horse, double pace, double fallback) {
        double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
        return speed <= 0.0 ? fallback : Math.sqrt(pace / speed);
    }
}
