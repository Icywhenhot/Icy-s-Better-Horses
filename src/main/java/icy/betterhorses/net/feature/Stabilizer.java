package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.HorseStabilizerLogic;
import icy.betterhorses.net.HorseStabilizerState;
import icy.betterhorses.net.IHorseData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.phys.Vec3;

public final class Stabilizer implements HorseFeature {

    private static final double HALF_OPEN_DESCENT_SPEED = -0.35D;
    private static final double MAX_DESCENT_SPEED = -0.125D;
    private static final double SMOOTHING = 0.35D;
    private static final double HALF_OPEN_SMOOTHING = 0.2D;

    private double fall;
    private double stepY;
    private double previousStepY;
    private double lastY = Double.NaN;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        boolean serverSide = !horse.level().isClientSide();
        boolean simulates = serverSide || horse.isLocalInstanceAuthoritative();

        if (simulates) {
            trackDescent(horse, data);
        }

        HorseStabilizerState state = simulates
                ? computeState(horse, data)
                : data.bh_getStabilizerState();

        if (simulates && (state == HorseStabilizerState.OPEN || state == HorseStabilizerState.HALF_OPEN)) {
            Vec3 motion = horse.getDeltaMovement();
            double targetSpeed = state == HorseStabilizerState.OPEN
                    ? MAX_DESCENT_SPEED
                    : HALF_OPEN_DESCENT_SPEED;
            double smoothing = state == HorseStabilizerState.OPEN
                    ? SMOOTHING
                    : HALF_OPEN_SMOOTHING;

            if (motion.y < targetSpeed) {
                double smoothedY = Mth.lerp(smoothing, motion.y, targetSpeed);
                if (smoothedY > targetSpeed) {
                    smoothedY = targetSpeed;
                }
                horse.setDeltaMovement(motion.x, smoothedY, motion.z);
                horse.hurtMarked = true;
            }
            if (state == HorseStabilizerState.OPEN) {
                horse.fallDistance = 0.0D;
            }
        }

        if (serverSide) {
            data.bh_setStabilizerState(state);
        }
    }

    private void trackDescent(AbstractHorse horse, IHorseData data) {
        double y = horse.getY();
        this.previousStepY = this.stepY;
        this.stepY = Double.isNaN(this.lastY) ? 0.0D : y - this.lastY;
        this.lastY = y;

        if (horse.onGround() || horse.isInWater() || horse.isInLava()
                || data.bh_getStabilizerState() == HorseStabilizerState.OPEN) {
            this.fall = 0.0D;
        } else if (this.stepY < 0.0D) {
            this.fall -= this.stepY;
        }
    }

    private HorseStabilizerState computeState(AbstractHorse horse, IHorseData data) {
        double verticalSpeed = Math.min(
                horse.getDeltaMovement().y,
                Math.min(this.stepY, this.previousStepY));
        float fallDistance = (float) Math.max(horse.fallDistance, this.fall);

        return HorseStabilizerLogic.computeState(
                hasStabilizerGear(data),
                horse.onGround(),
                horse.isInWater(),
                horse.isInLava(),
                horse.isPassenger(),
                verticalSpeed,
                fallDistance,
                data.bh_getStabilizerState());
    }

    public static boolean hasStabilizerGear(IHorseData data) {
        return BhConfig.stabilizerEnabled() && data.bh_hasStabilizerItem();
    }
}
