package icy.betterhorses.net.goal;

import icy.betterhorses.net.IHorseData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.UUID;

// Walks a horse off a block that is hurting it. It only ever walks: a horse with no way out stays
// where it is, and its owner is told.
public class HorseEscapeHazardGoal extends Goal {

    private static final double ESCAPE_SPEED = 1.3;
    private static final int ESCAPE_RANGE = 5;
    private static final int ESCAPE_HEIGHT = 4;
    private static final int SPOTS_PER_SEARCH = 10;
    private static final int SEARCH_INTERVAL_TICKS = 5;
    // this many searches in a row without a spot it can walk to means it is trapped
    private static final int TRAPPED_SEARCHES = 6;

    private final AbstractHorse horse;
    private boolean leaving;
    private int searchCooldown;
    private int failedSearches;
    private boolean toldTrapped;

    public HorseEscapeHazardGoal(AbstractHorse horse) {
        this.horse = horse;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !horse.isVehicle() && IHorseData.of(horse).bh_getHazardEscapeTicks() > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        leaving = false;
        searchCooldown = 0;
    }

    @Override
    public void tick() {
        IHorseData data = IHorseData.of(horse);
        data.bh_setHazardEscapeTicks(data.bh_getHazardEscapeTicks() - 1);

        if (leaving) {
            // there, or as near as it could get: being hurt again starts another run
            if (horse.getNavigation().isDone()) data.bh_setHazardEscapeTicks(0);
            return;
        }
        if (--searchCooldown > 0) return;
        searchCooldown = SEARCH_INTERVAL_TICKS;

        if (leave()) {
            leaving = true;
            failedSearches = 0;
            toldTrapped = false;
        } else if (++failedSearches >= TRAPPED_SEARCHES) {
            tellOwnerTrapped(data);
        }
    }

    // The spots offered are ones a horse will stand on, so never another block that would hurt it.
    private boolean leave() {
        for (int i = 0; i < SPOTS_PER_SEARCH; i++) {
            Vec3 spot = DefaultRandomPos.getPos(horse, ESCAPE_RANGE, ESCAPE_HEIGHT);
            if (spot == null) continue;
            Path path = horse.getNavigation().createPath(spot.x, spot.y, spot.z, 0);
            if (path != null && path.canReach() && horse.getNavigation().moveTo(path, ESCAPE_SPEED)) {
                return true;
            }
        }
        return false;
    }

    private void tellOwnerTrapped(IHorseData data) {
        if (toldTrapped) return;
        UUID ownerId = data.bh_getOwner();
        Player owner = ownerId == null ? null : horse.level().getPlayerByUUID(ownerId);
        if (owner == null) return;
        toldTrapped = true;
        // the fallback is what a client still on a jar without this key shows
        owner.sendSystemMessage(Component.translatableWithFallback("message.icys-better-horses.hazard.trapped",
                "%s is getting hurt and can't get away. It is at %s, %s, %s.",
                horse.getDisplayName(), horse.getBlockX(), horse.getBlockY(), horse.getBlockZ()));
    }

    @Override
    public void stop() {
        leaving = false;
        horse.getNavigation().stop();
    }
}
