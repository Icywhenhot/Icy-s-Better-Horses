package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FriesianPresence implements BreedAbility {

    private static final double RADIUS = 12.0D;
    private static final double MOUNT_RADIUS = 24.0D;
    private static final int INTERVAL = 10;

    private static final double DRESSAGE_RADIUS = 5.0D;
    private static final double DRESSAGE_PUSH = 1.1D;

    private boolean hadRider;
    private boolean wasStanding;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());

        if (rider != null && !hadRider) {
            calm(horse, rider, MOUNT_RADIUS, true, tier);
        }
        hadRider = rider != null;

        if (rider != null && horse.tickCount % INTERVAL == 0) {
            calm(horse, rider, RADIUS, false, tier);
        }

        boolean standing = horse.isStanding();
        if (tier >= 2 && standing && !wasStanding && dressage(horse)) {
            BhSurge.pulse(data, 0);
        }
        wasStanding = standing;
    }

    private boolean dressage(AbstractHorse horse) {
        Vec3 look = horse.getLookAngle();
        Vec3 dir = new Vec3(look.x, 0.0D, look.z).normalize();
        AABB box = horse.getBoundingBox().inflate(DRESSAGE_RADIUS);
        boolean pushed = false;
        for (LivingEntity target : horse.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == horse || horse.hasIndirectPassenger(target) || !(target instanceof Enemy)) {
                continue;
            }
            Vec3 to = target.position().subtract(horse.position());
            if (to.lengthSqr() < 1.0E-4D || dir.dot(to.normalize()) < 0.0D) {
                continue;
            }
            Vec3 push = new Vec3(to.x, 0.0D, to.z).normalize().scale(DRESSAGE_PUSH);
            target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.4D, push.z));
            target.hurtMarked = true;
            if (target instanceof Mob mob) {
                mob.setTarget(null);
            }
            pushed = true;
        }
        return pushed;
    }

    private void calm(AbstractHorse horse, Player rider, double radius,
                      boolean includeProvoked, int tier) {
        AABB box = horse.getBoundingBox().inflate(radius);
        for (Mob mob : horse.level().getEntitiesOfClass(Mob.class, box)) {
            boolean neutral = mob instanceof NeutralMob;
            if (!neutral && !(tier >= 1 && mob instanceof Enemy)) {
                continue;
            }
            LivingEntity target = mob.getTarget();
            if (target != rider && target != horse) {
                continue;
            }
            if (!includeProvoked) {
                LivingEntity attacker = mob.getLastHurtByMob();
                if (attacker == rider || attacker == horse) {
                    continue;
                }
            }
            mob.setTarget(null);
            if (mob instanceof NeutralMob angry) {
                angry.stopBeingAngry();
            }
        }
    }
}
