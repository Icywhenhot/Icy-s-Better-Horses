package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.feature.HorseCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.EnumSet;
import java.util.UUID;

public class DefendOwnerGoal extends Goal {

    private static final double CHASE_SPEED = 1.35;
    private static final double CHARGE_SPEED = 1.7;
    private static final double REACH_SQ = 6.0;
    private static final double CHARGE_SQ = 36.0;
    private static final double LEASH_SQ = 1024.0;
    private static final int SWING_COOLDOWN = 12;
    private static final float BREAK_OFF_HEALTH = 0.3F;

    private final AbstractHorse horse;
    private LivingEntity target;
    private int swing;
    private boolean charged;

    public DefendOwnerGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle() || !(horse.level() instanceof ServerLevel level)) {
            return false;
        }
        IHorseData data = IHorseData.of(horse);
        if (data.bh_getCommand() == HorseCommand.STAY) {
            return false;
        }
        UUID id = data.bh_getCombatTarget();
        if (id == null) {
            return false;
        }
        target = level.getEntity(id) instanceof LivingEntity found ? found : null;
        if (target == null || !target.isAlive() || horse.distanceToSqr(target) > LEASH_SQ) {
            data.bh_setCombatTarget(null);
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        IHorseData data = IHorseData.of(horse);
        if (horse.isVehicle() || data.bh_getCombatTarget() == null
                || data.bh_getCommand() == HorseCommand.STAY) {
            return false;
        }
        if (target == null || !target.isAlive()
                || horse.getHealth() < horse.getMaxHealth() * BREAK_OFF_HEALTH
                || horse.distanceToSqr(target) > LEASH_SQ) {
            data.bh_setCombatTarget(null);
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        if (target == null || !(horse.level() instanceof ServerLevel level)) {
            return;
        }
        double gap = horse.distanceToSqr(target);
        horse.getLookControl().setLookAt(target, 30.0F, 30.0F);
        horse.getNavigation().moveTo(target, gap > CHARGE_SQ ? CHARGE_SPEED : CHASE_SPEED);

        if (gap > REACH_SQ) {
            if (gap > CHARGE_SQ) {
                charged = false;
            }
            return;
        }
        if (!charged) {
            charged = true;
            swing = SWING_COOLDOWN;
            HorseCombat.chargeStrike(level, horse, IHorseData.of(horse), target);
            return;
        }
        if (swing > 0) {
            swing--;
            return;
        }
        HorseCombat.strike(level, horse, IHorseData.of(horse), target);
        swing = SWING_COOLDOWN;
    }

    @Override
    public void stop() {
        target = null;
        swing = 0;
        charged = false;
        horse.getNavigation().stop();
    }
}
