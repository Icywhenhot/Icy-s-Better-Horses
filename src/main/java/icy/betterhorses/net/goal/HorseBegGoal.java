package icy.betterhorses.net.goal;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class HorseBegGoal extends Goal {

    private static final double RANGE = 8.0D;
    private static final TargetingConditions WATCHERS = TargetingConditions.forNonCombat().range(RANGE);

    private final AbstractHorse horse;
    private @Nullable Player player;
    private int lookTime;
    private int calmUntil;

    public HorseBegGoal(AbstractHorse horse) {
        this.horse = horse;
    }

    @Override
    public boolean canUse() {
        if (horse.tickCount < calmUntil || !settled()) return false;
        player = horse.level().getNearestPlayer(WATCHERS, horse);
        return player != null && holdingTreat(player);
    }

    @Override
    public boolean canContinueToUse() {
        return player != null && player.isAlive()
                && lookTime > 0
                && horse.distanceToSqr(player) <= RANGE * RANGE
                && settled()
                && holdingTreat(player);
    }

    @Override
    public void start() {
        IHorseData.of(horse).bh_setBegSide(horse.getRandom().nextBoolean() ? 1 : -1);
        lookTime = adjustedTickDelay(60 + horse.getRandom().nextInt(80));
    }

    @Override
    public void stop() {
        IHorseData.of(horse).bh_setBegSide(0);
        player = null;
        calmUntil = horse.tickCount + 20 + horse.getRandom().nextInt(40);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (player == null) return;
        horse.getLookControl().setLookAt(player, 10.0F, horse.getMaxHeadXRot());
        lookTime--;
    }

    private boolean settled() {
        if (!horse.isAlive() || horse.isVehicle() || horse.isEating() || horse.isStanding()
                || !BhHorseKind.managed(horse)) {
            return false;
        }
        IHorseData data = IHorseData.of(horse);
        return data.bh_getSpookTicks() <= 0 && data.bh_getCombatTarget() == null;
    }

    private boolean holdingTreat(Player p) {
        for (InteractionHand hand : InteractionHand.values()) {
            if (horse.isFood(p.getItemInHand(hand))) return true;
        }
        return false;
    }
}
