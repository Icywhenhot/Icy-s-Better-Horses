package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.EnumSet;

public class HorseWanderBoundsGoal extends Goal {

    private static final double WANDER_HALF_EXTENT = 16.0D;
    private static final double RETURN_PADDING = 2.0D;
    private static final double RETURN_SPEED = 1.0D;
    private static final double MIN_TARGET_DISTANCE_SQ = 1.0D;
    private static final int REPATH_INTERVAL_TICKS = 20;

    private final AbstractHorse horse;
    private BlockPos wanderCenter;
    private int repathCooldown;

    public HorseWanderBoundsGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle()) return false;
        IHorseData data = (IHorseData) horse;
        if (!data.bh_isOwned() || data.bh_getCommand() != HorseCommand.WANDER) return false;
        wanderCenter = data.bh_getWanderCenter();
        if (wanderCenter == null) {
            data.bh_setWanderCenter(horse.blockPosition());
            return false;
        }
        return bh_isOutsideBounds(wanderCenter, WANDER_HALF_EXTENT);
    }

    @Override
    public boolean canContinueToUse() {
        IHorseData data = (IHorseData) horse;
        if (data.bh_getCommand() != HorseCommand.WANDER) return false;
        wanderCenter = data.bh_getWanderCenter();
        return wanderCenter != null && bh_isOutsideBounds(wanderCenter, WANDER_HALF_EXTENT - RETURN_PADDING);
    }

    @Override
    public void start() {
        repathCooldown = 0;
        bh_repathTowardBounds();
    }

    @Override
    public void tick() {
        if (wanderCenter == null) return;
        if (--repathCooldown <= 0 || horse.getNavigation().isDone()) {
            bh_repathTowardBounds();
        }
    }

    @Override
    public void stop() {
        wanderCenter = null;
        repathCooldown = 0;
        horse.getNavigation().stop();
    }

    private boolean bh_isOutsideBounds(BlockPos center, double halfExtent) {
        double centerX = center.getX() + 0.5D;
        double centerZ = center.getZ() + 0.5D;
        return Math.abs(horse.getX() - centerX) > halfExtent
                || Math.abs(horse.getZ() - centerZ) > halfExtent;
    }

    private void bh_repathTowardBounds() {
        if (wanderCenter == null) return;

        double centerX = wanderCenter.getX() + 0.5D;
        double centerZ = wanderCenter.getZ() + 0.5D;
        double targetHalfExtent = Math.max(0.0D, WANDER_HALF_EXTENT - RETURN_PADDING);
        double targetX = bh_clamp(horse.getX(), centerX - targetHalfExtent, centerX + targetHalfExtent);
        double targetZ = bh_clamp(horse.getZ(), centerZ - targetHalfExtent, centerZ + targetHalfExtent);
        double dx = targetX - horse.getX();
        double dz = targetZ - horse.getZ();

        if (dx * dx + dz * dz < MIN_TARGET_DISTANCE_SQ) {
            targetX = centerX;
            targetZ = centerZ;
        }

        if (!horse.getNavigation().moveTo(targetX, wanderCenter.getY(), targetZ, RETURN_SPEED)) {
            horse.getNavigation().moveTo(centerX, wanderCenter.getY(), centerZ, RETURN_SPEED);
        }

        repathCooldown = REPATH_INTERVAL_TICKS;
    }

    private double bh_clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
