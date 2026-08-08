package icy.betterhorses.net.goal;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModTicketTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.EnumSet;

public class HorseReturnHomeGoal extends Goal {

    private static final double RETURN_SPEED = 1.0;
    private static final double ARRIVED_DIST_SQ = 4.0; // 2 blocks
    // the horse walks off toward home for this long so the departure looks natural
    private static final double NATURAL_WALK_DISTANCE = 32.0;
    private static final double NATURAL_WALK_DIST_SQ = NATURAL_WALK_DISTANCE * NATURAL_WALK_DISTANCE;
    private static final int TICKET_RADIUS = 3; // keeps the horse's own chunk entity-ticking with a 1-chunk margin
    private static final int TICKET_REFRESH_INTERVAL_TICKS = 20;
    private static final int STUCK_CHECK_INTERVAL_TICKS = 100;
    private static final double STUCK_MIN_PROGRESS_SQ = 2.25; // 1.5 blocks of net movement per check window

    private final AbstractHorse horse;

    private Vec3 walkStartPos;
    private int ticketRefreshCooldown;
    private ChunkPos ticketChunk;
    private int stuckCheckCooldown;
    private Vec3 lastProgressPos;

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
        walkStartPos = horse.position();
        ticketRefreshCooldown = 0;
        ticketChunk = null;
        stuckCheckCooldown = STUCK_CHECK_INTERVAL_TICKS;
        lastProgressPos = horse.position();
        refreshChunkTicket();
        navigateHome();
    }

    @Override
    public void tick() {
        ChunkPos current = horse.chunkPosition();
        if (--ticketRefreshCooldown <= 0 || !current.equals(ticketChunk)) {
            refreshChunkTicket();
        }
        if (checkStuck()) return;
        if (hasWalkedNaturalLeg()) {
            teleportHome();
            return;
        }
        if (horse.getNavigation().isDone()) {
            navigateHome();
        }
    }

    @Override
    public void stop() {
        // the chunk ticket is left to expire on its own shortly after
        walkStartPos = null;
        ticketChunk = null;
        lastProgressPos = null;
    }

    private void navigateHome() {
        BlockPos home = ((IHorseData) horse).bh_getHome();
        if (home == null) return;
        Vec3 homeCenter = Vec3.atBottomCenterOf(home);
        Vec3 target = homeCenter;
        if (horse.distanceToSqr(homeCenter) > NATURAL_WALK_DIST_SQ) {
            // home is far: aim the walk leg at a nearby waypoint in home's direction instead of home itself
            Vec3 direction = homeCenter.subtract(horse.position()).normalize();
            target = horse.position().add(direction.scale(NATURAL_WALK_DISTANCE));
        }
        boolean reached = horse.getNavigation().moveTo(target.x, target.y, target.z, RETURN_SPEED);
        if (!reached) {
            // teleport fallback when pathfinding fails (e.g
            teleportHome();
        }
    }

    // true once the horse has covered the natural-looking stretch and home is still far off
    private boolean hasWalkedNaturalLeg() {
        if (walkStartPos == null || horse.isVehicle() || horse.isLeashed()) return false;
        if (horse.position().distanceToSqr(walkStartPos) < NATURAL_WALK_DIST_SQ) return false;
        BlockPos home = ((IHorseData) horse).bh_getHome();
        return home != null && horse.distanceToSqr(Vec3.atBottomCenterOf(home)) > NATURAL_WALK_DIST_SQ;
    }

    // self-sustaining chunk ticket: keeps the horse ticking so the walk-off leg (and the teleport
    private void refreshChunkTicket() {
        if (!(horse.level() instanceof ServerLevel serverLevel)) return;
        ticketChunk = horse.chunkPosition();
        ticketRefreshCooldown = TICKET_REFRESH_INTERVAL_TICKS;
        serverLevel.getChunkSource().addTicketWithRadius(ModTicketTypes.HORSE_TASK, ticketChunk, TICKET_RADIUS);
    }

    // a horse that is boxed in (fences, pens, water edges) never reports a failed path
    private boolean checkStuck() {
        if (horse.isVehicle() || horse.isLeashed()) {
            stuckCheckCooldown = STUCK_CHECK_INTERVAL_TICKS;
            lastProgressPos = horse.position();
            return false;
        }
        if (--stuckCheckCooldown > 0) return false;
        Vec3 current = horse.position();
        boolean stuck = lastProgressPos != null && current.distanceToSqr(lastProgressPos) < STUCK_MIN_PROGRESS_SQ;
        stuckCheckCooldown = STUCK_CHECK_INTERVAL_TICKS;
        lastProgressPos = current;
        if (stuck) {
            teleportHome();
        }
        return stuck;
    }

    private void teleportHome() {
        BlockPos home = ((IHorseData) horse).bh_getHome();
        if (home == null) return;
        if (horse.level() instanceof ServerLevel serverLevel) {
            // make sure the destination is loaded so the horse lands and gets saved there properly
            serverLevel.getChunkSource().addTicketWithRadius(ModTicketTypes.HORSE_TASK, ChunkPos.containing(home), 1);
        }
        horse.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        ((IHorseData) horse).bh_setCommand(HorseCommand.STAY);
    }
}
