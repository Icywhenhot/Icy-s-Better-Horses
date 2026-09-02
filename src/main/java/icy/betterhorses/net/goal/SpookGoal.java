package icy.betterhorses.net.goal;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SpookGoal extends Goal {

    private static final double BOLT_SPEED = 1.6;
    private static final int REPATH = 20;

    private final AbstractHorse horse;
    private int repath;

    public SpookGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return IHorseData.of(horse).bh_getSpookTicks() > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        IHorseData data = IHorseData.of(horse);
        data.bh_setSpookTicks(data.bh_getSpookTicks() - 1);

        if (repath > 0) {
            repath--;
            return;
        }
        Vec3 away = DefaultRandomPos.getPos(horse, 16, 7);
        if (away != null) {
            horse.getNavigation().moveTo(away.x, away.y, away.z, BOLT_SPEED);
        }
        repath = REPATH;
    }

    @Override
    public void stop() {
        repath = 0;
        horse.getNavigation().stop();
    }
}
