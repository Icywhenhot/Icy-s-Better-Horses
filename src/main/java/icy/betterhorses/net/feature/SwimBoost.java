package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.phys.Vec3;

public final class SwimBoost implements HorseFeature {

    private static final double HORIZONTAL_BOOST = 1.125D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (!horse.isInWater() || !horse.isVehicle()) {
            return;
        }
        Vec3 motion = horse.getDeltaMovement();
        if (motion.x * motion.x + motion.z * motion.z < 1.0E-6D) {
            return;
        }
        horse.setDeltaMovement(
                motion.x * HORIZONTAL_BOOST,
                motion.y,
                motion.z * HORIZONTAL_BOOST);
    }
}
