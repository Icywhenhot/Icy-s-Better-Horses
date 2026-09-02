package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Intimidation implements BreedAbility {

    private final BrickBreak charge = new BrickBreak();

    private static final int SWEEP = 5;
    private static final int FEAR_TICKS = 100;
    private static final int SNORT_COOLDOWN = 40;
    private static final int STOMP_TICKS = 10;
    private static final int WATCH_TICKS = 25;
    private static final double RADIUS = 10.0D;
    private static final double WIDE_RADIUS = 16.0D;
    private static final double WARD_REACH_SQ = 256.0D;
    private static final double BACKOFF = 0.42D;
    private static final double STILL_SPEED_SQ = 0.0025D;

    private final Map<UUID, Integer> feared = new HashMap<>();
    private int snortCooldown;
    private @Nullable Mob watching;
    private int watchTicks;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        charge.tick(horse, data, state);
        if (snortCooldown > 0) {
            snortCooldown--;
        }
        if (watchTicks > 0) {
            watchTicks--;
            if (watching != null && watching.isAlive()) {
                horse.getLookControl().setLookAt(watching, 45.0F, 45.0F);
            } else {
                watchTicks = 0;
                watching = null;
            }
        }
        if (!(horse.level() instanceof ServerLevel level) || horse.tickCount % SWEEP != 0) {
            return;
        }

        LivingEntity ward = ward(horse, data);
        if (ward == null) {
            feared.clear();
            return;
        }

        double radius = BhHorseTraits.bondTier(data.bh_getBond()) >= 1 ? WIDE_RADIUS : RADIUS;
        AABB box = horse.getBoundingBox().inflate(radius);
        Mob fresh = null;
        int held = 0;

        UUID hunting = data.bh_getCombatTarget();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
            if (!(mob instanceof Enemy)) {
                continue;
            }
            if (mob.getUUID().equals(hunting) || ward.getLastHurtByMob() == mob) {
                feared.remove(mob.getUUID());
                continue;
            }
            boolean known = feared.containsKey(mob.getUUID());
            if (!known) {
                if (mob.getTarget() != ward
                        || mob.getLastHurtByMob() == ward || mob.getLastHurtByMob() == horse) {
                    continue;
                }
                fresh = mob;
            }
            feared.put(mob.getUUID(), FEAR_TICKS);
            mob.setTarget(null);
            backAway(horse, mob);
            held++;
        }

        feared.entrySet().removeIf(entry -> {
            int left = entry.getValue() - SWEEP;
            entry.setValue(left);
            return left <= 0 || level.getEntity(entry.getKey()) == null;
        });

        if (fresh != null || (held > 0 && snortCooldown <= 0)) {
            warn(horse, data, fresh != null ? fresh : ward);
        }
    }

    private void warn(AbstractHorse horse, IHorseData data, LivingEntity at) {
        snortCooldown = SNORT_COOLDOWN;
        if (at instanceof Mob threat) {
            watching = threat;
            watchTicks = WATCH_TICKS;
        }
        horse.getLookControl().setLookAt(at, 45.0F, 45.0F);
        horse.level().playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                ModSounds.HORSE_ANGRY_SNORT, horse.getSoundSource(), 0.8F, 0.8F);
        if (mayStomp(horse)) {
            data.bh_setStompTicks(STOMP_TICKS);
        }
    }

    private static boolean mayStomp(AbstractHorse horse) {
        return horse.getControllingPassenger() == null
                || horse.getKnownMovement().horizontalDistanceSqr() < STILL_SPEED_SQ;
    }

    private static @Nullable LivingEntity ward(AbstractHorse horse, IHorseData data) {
        if (horse.getControllingPassenger() instanceof Player rider) {
            return rider;
        }
        UUID owner = data.bh_getOwner();
        if (owner == null) {
            return null;
        }
        Player nearby = horse.level().getPlayerByUUID(owner);
        return nearby != null && nearby.distanceToSqr(horse) <= WARD_REACH_SQ ? nearby : null;
    }

    private static void backAway(AbstractHorse horse, Mob mob) {
        Vec3 away = mob.position().subtract(horse.position());
        if (away.horizontalDistanceSqr() < 1.0E-4D) {
            return;
        }
        Vec3 push = new Vec3(away.x, 0.0D, away.z).normalize().scale(BACKOFF);
        mob.getNavigation().moveTo(
                mob.getX() + push.x * 8.0D, mob.getY(), mob.getZ() + push.z * 8.0D, 1.2D);
    }
}
