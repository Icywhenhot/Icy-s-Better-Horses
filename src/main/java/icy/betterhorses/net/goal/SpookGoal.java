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
    private static final int TRIES = 10;

    private final AbstractHorse horse;
    private int repath;
    private Vec3 from = Vec3.ZERO;

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
    public void start() {
        from = horse.position();
    }

    @Override
    public void tick() {
        IHorseData data = IHorseData.of(horse);
        data.bh_setSpookTicks(data.bh_getSpookTicks() - 1);

        if (repath > 0) {
            repath--;
            return;
        }
        boolean fled = horse.position().distanceToSqr(from) > 1.0D;
        for (int i = 0; i < TRIES; i++) {
            Vec3 away = fled ? DefaultRandomPos.getPosAway(horse, 16, 7, from) : DefaultRandomPos.getPos(horse, 16, 7);
            if (away != null && horse.getNavigation().moveTo(away.x, away.y, away.z, BOLT_SPEED)) {
                repath = REPATH;
                return;
            }
        }
        Vec3 dir = fled ? horse.position().subtract(from) : Vec3.directionFromRotation(0.0F, horse.getRandom().nextFloat() * 360.0F);
        dir = new Vec3(dir.x, 0.0D, dir.z).normalize().scale(4.0D);
        horse.getMoveControl().setWantedPosition(horse.getX() + dir.x, horse.getY(), horse.getZ() + dir.z, BOLT_SPEED);
        repath = 5;
    }

    @Override
    public void stop() {
        repath = 0;
        horse.getNavigation().stop();
    }
}
