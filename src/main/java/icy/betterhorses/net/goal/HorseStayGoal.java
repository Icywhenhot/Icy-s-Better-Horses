package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.EnumSet;

public class HorseStayGoal extends Goal {

    private final AbstractHorse horse;

    public HorseStayGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (horse.isVehicle()) return false;
        IHorseData data = (IHorseData) horse;
        return data.bh_isOwned() && data.bh_getCommand() == HorseCommand.STAY;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        horse.getNavigation().stop();
    }

    @Override
    public void tick() {
        horse.getNavigation().stop();
    }
}
