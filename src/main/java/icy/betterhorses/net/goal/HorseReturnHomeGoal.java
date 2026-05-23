package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.EnumSet;

public class HorseReturnHomeGoal extends Goal {

    private static final double RETURN_SPEED = 1.0;
    private static final double ARRIVED_DIST_SQ = 4.0; // 2 blocks

    private final AbstractHorse horse;

    public HorseReturnHomeGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle()) return false;
        IHorseData data = (IHorseData) horse;
        if (!data.bh_isOwned() || data.bh_getCommand() != HorseCommand.RETURN_HOME) return false;
        BlockPos home = data.bh_getHome();
        if (home == null) {
            data.bh_setCommand(HorseCommand.STAY);
            return false;
        }
        return horse.distanceToSqr(Vec3.atBottomCenterOf(home)) > ARRIVED_DIST_SQ;
    }

    @Override
    public boolean canContinueToUse() {
        IHorseData data = (IHorseData) horse;
        if (data.bh_getCommand() != HorseCommand.RETURN_HOME) return false;
        BlockPos home = data.bh_getHome();
        if (home == null) return false;
        if (horse.distanceToSqr(Vec3.atBottomCenterOf(home)) <= ARRIVED_DIST_SQ) {
            data.bh_setCommand(HorseCommand.STAY);
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        navigateHome();
    }

    @Override
    public void tick() {
        if (horse.getNavigation().isDone()) {
            navigateHome();
        }
    }

    private void navigateHome() {
        BlockPos home = ((IHorseData) horse).bh_getHome();
        if (home == null) return;
        boolean reached = horse.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, RETURN_SPEED);
        if (!reached) {
            // Teleport fallback when pathfinding fails (e.g. unloaded chunks, obstacles)
            horse.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
            ((IHorseData) horse).bh_setCommand(HorseCommand.STAY);
        }
    }
}
