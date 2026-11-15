package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SwimBoost implements HorseFeature {

    private static final double HORIZONTAL_BOOST = 1.125D;

    private static final double RIDDEN_SWIM_CAP = 0.3D;
    private static final double JUMP_LAUNCH = 0.2D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (!horse.isInWater() || horse instanceof SkeletonHorse || data.bh_getBreed().isSkeleton() || sinks(data)) {
            return;
        }
        Vec3 motion = horse.getDeltaMovement();
        if (horse.isVehicle() && !swimsFast(horse) && motion.horizontalDistanceSqr() >= 1.0E-6D) {
            horse.setDeltaMovement(motion.x * HORIZONTAL_BOOST, motion.y, motion.z * HORIZONTAL_BOOST);
            capRiddenCurrent(horse);
            motion = horse.getDeltaMovement();
        }
        if (horse.onGround()) {
            return;
        }
        if (horse.horizontalCollision) {
            horse.setDeltaMovement(motion.x, Math.max(motion.y, 0.15D), motion.z);
            horse.resetFallDistance();
            return;
        }
        double depth = horse.getFluidHeight(FluidTags.WATER);
        if (depth > 1.0D && motion.y < 0.08D) {
            double lift = Math.min((depth - 1.0D) * 0.1D, 0.08D);
            horse.setDeltaMovement(motion.x, Math.min(motion.y + lift, 0.08D), motion.z);
            horse.resetFallDistance();
        }
    }

    private static void capRiddenCurrent(AbstractHorse horse) {
        Vec3 motion = horse.getDeltaMovement();
        if (!(horse.getControllingPassenger() instanceof Player) || !horse.isLocalInstanceAuthoritative()
                || motion.y > JUMP_LAUNCH) {
            return;
        }
        double speed = motion.horizontalDistance();
        if (speed > RIDDEN_SWIM_CAP) {
            double scale = RIDDEN_SWIM_CAP / speed;
            horse.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
        }
    }

    private static boolean swimsFast(AbstractHorse horse) {
        return horse instanceof SkeletonHorse
                || IHorseData.of(horse).bh_getBreed().isSkeleton()
                || sinks(IHorseData.of(horse))
                || horse.hasEffect(MobEffects.DOLPHINS_GRACE)
                || horse.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) > 0.0D;
    }

    private static boolean sinks(IHorseData data) {
        return data.bh_getBreed().isDrowned() && BhAbility.DROWNED_SWIM.on();
    }
}
