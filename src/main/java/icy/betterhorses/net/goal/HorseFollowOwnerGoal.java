package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

public class HorseFollowOwnerGoal extends Goal {

    private static final double FOLLOW_SPEED = 1.2;
    private static final double STOP_DIST_SQ = 9.0; // 3 blocks

    private final AbstractHorse horse;
    private Player owner;

    public HorseFollowOwnerGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle()) return false;
        IHorseData data = (IHorseData) horse;
        if (!data.bh_isOwned() || data.bh_getCommand() != HorseCommand.FOLLOW) return false;
        UUID ownerId = data.bh_getOwner();
        owner = horse.level().getPlayerByUUID(ownerId);
        return owner != null && horse.distanceToSqr(owner) > STOP_DIST_SQ;
    }

    @Override
    public boolean canContinueToUse() {
        if (owner == null || !owner.isAlive()) return false;
        IHorseData data = (IHorseData) horse;
        return data.bh_getCommand() == HorseCommand.FOLLOW
                && horse.distanceToSqr(owner) > STOP_DIST_SQ;
    }

    @Override
    public void tick() {
        if (owner != null) {
            horse.getNavigation().moveTo(owner, FOLLOW_SPEED);
        }
    }

    @Override
    public void stop() {
        owner = null;
        horse.getNavigation().stop();
    }
}
