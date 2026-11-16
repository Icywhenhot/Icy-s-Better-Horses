package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.HorseCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class Guardian {

    private static final double RADIUS = 12.0D;

    private Guardian() {}

    public static void hunt(AbstractHorse horse, IHorseData data) {
        if (horse.isBaby() || horse.isVehicle() || data.bh_getCombatTarget() != null) {
            return;
        }
        LivingEntity prey = null;
        double best = RADIUS * RADIUS;
        for (LivingEntity e : horse.level().getEntitiesOfClass(LivingEntity.class, horse.getBoundingBox().inflate(RADIUS),
                e -> (e instanceof Wolf || e instanceof Fox) && e.isAlive() && HorseCombat.mayTarget(e))) {
            double d = horse.distanceToSqr(e);
            if (d < best) {
                best = d;
                prey = e;
            }
        }
        if (prey != null) {
            data.bh_setCombatTarget(prey.getUUID());
        }
    }
}
