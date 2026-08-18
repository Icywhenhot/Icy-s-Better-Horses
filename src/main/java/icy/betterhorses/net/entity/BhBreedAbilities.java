package icy.betterhorses.net.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BhBreedAbilities {

    public static final double DEFAULT_SENSE_RADIUS = 12.0D;

    private BhBreedAbilities() {}

    public static @Nullable Player rider(AbstractHorse horse) {
        return horse.getControllingPassenger() instanceof Player player ? player : null;
    }

    public static boolean isRiddenOnServer(AbstractHorse horse) {
        return !horse.level().isClientSide() && rider(horse) != null;
    }

    public static final class MovementSampler {

        private static final double SMOOTHING = 0.7D;

        private double lastX;
        private double lastY;
        private double lastZ;
        private boolean primed;
        private double smoothedSpeed;
        private double verticalChange;

        public void sample(AbstractHorse horse) {
            double dx = horse.getX() - lastX;
            double dy = horse.getY() - lastY;
            double dz = horse.getZ() - lastZ;
            lastX = horse.getX();
            lastY = horse.getY();
            lastZ = horse.getZ();

            if (!primed) {
                primed = true;
                return;
            }

            smoothedSpeed = smoothedSpeed * SMOOTHING + Math.sqrt(dx * dx + dz * dz) * (1.0D - SMOOTHING);
            verticalChange = Math.abs(dy);
        }

        public boolean primed() {
            return primed;
        }

        public boolean isRunningFlat() {
            return primed && smoothedSpeed > MIN_RUN_SPEED && verticalChange < LEVEL_GROUND_TOLERANCE;
        }
    }

    private static final double LEVEL_GROUND_TOLERANCE = 0.35D;

    private static final double MIN_RUN_SPEED = 0.08D;

    public static List<LivingEntity> hostilesNearby(AbstractHorse horse, double radius) {
        AABB box = horse.getBoundingBox().inflate(radius);
        return horse.level().getEntitiesOfClass(LivingEntity.class, box, entity ->
                entity instanceof Enemy && entity.isAlive() && horse.distanceToSqr(entity) <= radius * radius);
    }

    public static void applyQuietEffect(
            LivingEntity target,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            int durationTicks,
            int amplifier) {
        MobEffectInstance existing = target.getEffect(effect);
        if (existing != null && existing.getAmplifier() > amplifier) {
            return;
        }
        if (existing != null && existing.getAmplifier() == amplifier && existing.getDuration() > durationTicks) {
            return;
        }
        target.addEffect(new MobEffectInstance(effect, durationTicks, amplifier, false, false, true));
    }

    public static void grantSpeed(LivingEntity target, int durationTicks, int amplifier) {
        applyQuietEffect(target, MobEffects.SPEED, durationTicks, amplifier);
    }

    public static void grantResistance(LivingEntity target, int durationTicks) {
        applyQuietEffect(target, MobEffects.RESISTANCE, durationTicks, 0);
    }

    public static boolean startGlowing(net.minecraft.world.entity.Entity entity) {
        if (entity.isCurrentlyGlowing()) {
            return false;
        }
        entity.setGlowingTag(true);
        return true;
    }

    public static boolean isDarkOutside(AbstractHorse horse) {
        return horse.level() instanceof ServerLevel level
                && (!level.isBrightOutside() || level.isThundering());
    }

}
