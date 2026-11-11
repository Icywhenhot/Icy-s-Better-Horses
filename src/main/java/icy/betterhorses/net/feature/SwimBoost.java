package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
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
        if (!horse.isInWater() || !horse.isVehicle() || swimsFast(horse)) {
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
        capRiddenCurrent(horse);
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
                || horse.hasEffect(MobEffects.DOLPHINS_GRACE)
                || horse.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY) > 0.0D;
    }
}
