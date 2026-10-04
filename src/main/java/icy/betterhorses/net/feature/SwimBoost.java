package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhWagonHitch;
import icy.betterhorses.net.BhWaterline;
import icy.betterhorses.net.IHorseData;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;

public final class SwimBoost implements HorseFeature {

    private static final double HORIZONTAL_BOOST = 1.125D;

    private static final double WATERLINE = 1.0D;
    private static final double LIFT_PER_BLOCK = 0.10D;
    private static final double LIFT_CAP = 0.08D;

    private static final double STEP_RISE = 0.15D;

    private static final double HITCHED_DRAFT = 0.6D;
    private static final double HITCHED_EASE = 0.25D;
    private static final double HITCHED_MAX_RISE = 0.15D;
    private static final double WAGON_GRAVITY = 0.04D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (!horse.isInWater()) {
            return;
        }

        Vec3 motion = horse.getDeltaMovement();
        if (horse.isVehicle() && motion.x * motion.x + motion.z * motion.z >= 1.0E-6D) {
            motion = new Vec3(motion.x * HORIZONTAL_BOOST, motion.y, motion.z * HORIZONTAL_BOOST);
            horse.setDeltaMovement(motion);
        }

        if (horse.onGround()) {
            return;
        }

        if (BhWagonHitch.hitched(horse)) {
            double surface = BhWaterline.surface(horse.level(), horse.getX(), horse.getY(), horse.getZ());
            if (!Double.isNaN(surface)) {
                double rise = Mth.clamp((surface - HITCHED_DRAFT - horse.getY()) * HITCHED_EASE,
                        -HITCHED_MAX_RISE, HITCHED_MAX_RISE);
                horse.setDeltaMovement(motion.x, rise + WAGON_GRAVITY, motion.z);
                horse.resetFallDistance();
                horse.setOnGround(true);
            }
            return;
        }

        if (horse.horizontalCollision) {
            horse.setDeltaMovement(motion.x, Math.max(motion.y, STEP_RISE), motion.z);
            horse.resetFallDistance();
            return;
        }

        double depth = horse.getFluidHeight(FluidTags.WATER);
        if (depth <= WATERLINE) {
            return;
        }

        double lift = Math.min((depth - WATERLINE) * LIFT_PER_BLOCK, LIFT_CAP);
        horse.setDeltaMovement(motion.x, Math.min(motion.y + lift, LIFT_CAP), motion.z);
        horse.resetFallDistance();
    }
}
