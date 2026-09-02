package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.EnumSet;
import java.util.UUID;

public class PackmateFollowGoal extends Goal {

    private static final double FOLLOW_SPEED = 1.35;
    private static final double STOP_DIST_SQ = 16.0;

    private final AbstractHorse horse;
    private AbstractHorse lead;

    public PackmateFollowGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle() || !(horse.level() instanceof ServerLevel level)) return false;
        IHorseData data = IHorseData.of(horse);
        if (data.bh_getCommand() != HorseCommand.PAIR) return false;
        UUID id = data.bh_getPairedTo();
        if (id == null) return false;
        lead = level.getEntity(id) instanceof AbstractHorse found ? found : null;
        return lead != null && lead.isAlive() && horse.distanceToSqr(lead) > STOP_DIST_SQ;
    }

    @Override
    public boolean canContinueToUse() {
        if (lead == null || !lead.isAlive()) return false;
        IHorseData data = IHorseData.of(horse);
        return data.bh_getCommand() == HorseCommand.PAIR
                && horse.distanceToSqr(lead) > STOP_DIST_SQ;
    }

    @Override
    public void tick() {
        if (lead != null) {
            horse.getNavigation().moveTo(lead, FOLLOW_SPEED);
        }
    }

    @Override
    public void stop() {
        lead = null;
        horse.getNavigation().stop();
    }
}
