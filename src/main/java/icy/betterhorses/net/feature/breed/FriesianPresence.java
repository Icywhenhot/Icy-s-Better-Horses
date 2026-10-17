package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.ICalmable;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FriesianPresence implements BreedAbility {

    private static final double RADIUS = 12.0D;
    private static final double MOUNT_RADIUS = 24.0D;
    private static final int INTERVAL = 10;
    private static final int CALM_TICKS = 30;

    private static final double DRESSAGE_RADIUS = 5.0D;
    private static final double DRESSAGE_PUSH = 1.1D;

    private final Set<UUID> calmed = new HashSet<>();
    private boolean hadRider;
    private boolean wasStanding;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());

        boolean soothes = BhAbility.FRIESIAN_CALM.on() && horse.level() instanceof ServerLevel;
        if ((!soothes || rider == null) && !calmed.isEmpty()) {
            release(horse);
        }
        boolean fresh = false;
        if (soothes && rider != null && !hadRider) {
            fresh = calm(horse, rider, MOUNT_RADIUS, true, tier);
        }
        hadRider = rider != null;

        if (soothes && rider != null && horse.tickCount % INTERVAL == 0) {
            fresh |= calm(horse, rider, RADIUS, false, tier);
        }
        if (fresh) {
            BhSurge.pulse(data, 0, 1);
        }

        boolean standing = horse.isStanding();
        if (tier >= 2 && BhAbility.FRIESIAN_DRESSAGE.on()
                && standing && !wasStanding && dressage(horse)) {
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

    private boolean calm(AbstractHorse horse, Player rider, double radius,
                         boolean includeProvoked, int tier) {
        boolean stopped = false;
        long until = horse.level().getGameTime() + CALM_TICKS;
        if (horse.level() instanceof ServerLevel level) {
            calmed.removeIf(id -> {
                ICalmable calmable = ICalmable.of(level.getEntity(id));
                return calmable == null || !calmable.bh_calmedBy(rider.getUUID());
            });
        }
        AABB box = horse.getBoundingBox().inflate(radius);
        for (Mob mob : horse.level().getEntitiesOfClass(Mob.class, box)) {
            boolean neutral = mob instanceof NeutralMob;
            if (!neutral && !(tier >= 1 && mob instanceof Enemy) || !(mob instanceof ICalmable calmable)) {
                continue;
            }
            if (calmable.bh_calmedBy(rider.getUUID())) {
                calmable.bh_calm(rider.getUUID(), horse.getUUID(), until);
                continue;
            }
            if (!aimsAt(mob, rider, horse)) {
                continue;
            }
            if (!includeProvoked) {
                LivingEntity attacker = mob.getLastHurtByMob();
                if (attacker == rider || attacker == horse) {
                    continue;
                }
            }
            calmable.bh_calm(rider.getUUID(), horse.getUUID(), until);
            calmed.add(mob.getUUID());
            soothe(mob, rider, horse);
            stopped = true;
        }
        return stopped;
    }

    private void release(AbstractHorse horse) {
        if (horse.level() instanceof ServerLevel level) {
            for (UUID id : calmed) {
                ICalmable calmable = ICalmable.of(level.getEntity(id));
                if (calmable != null) {
                    calmable.bh_releaseCalm();
                }
            }
        }
        calmed.clear();
    }

    private static boolean aimsAt(Mob mob, Player rider, AbstractHorse horse) {
        LivingEntity target = mob.getTarget();
        if (target == rider || target == horse) {
            return true;
        }
        LivingEntity remembered = brainTarget(mob.getBrain());
        return remembered == rider || remembered == horse;
    }

    private static void soothe(Mob mob, Player rider, AbstractHorse horse) {
        mob.setTarget(null);
        if (mob instanceof NeutralMob angry) {
            angry.stopBeingAngry();
        }
        Brain<?> brain = mob.getBrain();
        LivingEntity remembered = brainTarget(brain);
        if (remembered == rider || remembered == horse) {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }

    private static LivingEntity brainTarget(Brain<?> brain) {
        if (!brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
            return null;
        }
        return brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }
}
