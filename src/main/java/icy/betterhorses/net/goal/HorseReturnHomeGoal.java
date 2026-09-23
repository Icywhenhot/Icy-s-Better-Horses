package icy.betterhorses.net.goal;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModTicketTypes;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.EnumSet;

public class HorseReturnHomeGoal extends Goal {

    private static final double RETURN_SPEED = 1.0;
    private static final double ARRIVED_DIST_SQ = 4.0;
    private static final double NATURAL_WALK_DISTANCE = 32.0;
    private static final double NATURAL_WALK_DIST_SQ = NATURAL_WALK_DISTANCE * NATURAL_WALK_DISTANCE;
    private static final int TICKET_RADIUS = 3;
    private static final int TICKET_REFRESH_INTERVAL_TICKS = 20;
    private static final int STUCK_CHECK_INTERVAL_TICKS = 100;
    private static final double STUCK_MIN_PROGRESS_SQ = 2.25;

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
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_isOwned() || !data.bh_getCommand().equals(BhContent.COMMAND_RETURN_HOME.getKey())) return false;
        BlockPos home = data.bh_getHome();
        if (home == null) {
            data.bh_setCommand(BhContent.COMMAND_STAY.getKey());
            return false;
        }
        if (!homeIsHere(data)) return false;
        return horse.distanceToSqr(Vec3.atBottomCenterOf(home)) > ARRIVED_DIST_SQ;
    }

    @Override
    public boolean canContinueToUse() {
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getCommand().equals(BhContent.COMMAND_RETURN_HOME.getKey())) return false;
        BlockPos home = data.bh_getHome();
        if (home == null || !homeIsHere(data)) return false;
        if (horse.distanceToSqr(Vec3.atBottomCenterOf(home)) <= ARRIVED_DIST_SQ) {
            data.bh_setCommand(BhContent.COMMAND_STAY.getKey());
            return false;
        }
        return true;
    }

    private boolean homeIsHere(IHorseData data) {
        ResourceKey<Level> dim = data.bh_getHomeDimension();
        return dim == null || dim.equals(horse.level().dimension());
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
        walkStartPos = null;
        ticketChunk = null;
        lastProgressPos = null;
    }

    private void navigateHome() {
        BlockPos home = IHorseData.of(horse).bh_getHome();
        if (home == null) return;
        Vec3 homeCenter = Vec3.atBottomCenterOf(home);
        Vec3 target = homeCenter;
        if (horse.distanceToSqr(homeCenter) > NATURAL_WALK_DIST_SQ) {
            Vec3 direction = homeCenter.subtract(horse.position()).normalize();
            target = horse.position().add(direction.scale(NATURAL_WALK_DISTANCE));
        }
        boolean reached = horse.getNavigation().moveTo(target.x, target.y, target.z, RETURN_SPEED);
        if (!reached) {
            teleportHome();
        }
    }

    private boolean hasWalkedNaturalLeg() {
        if (walkStartPos == null || horse.isVehicle() || horse.isLeashed()) return false;
        if (horse.position().distanceToSqr(walkStartPos) < NATURAL_WALK_DIST_SQ) return false;
        BlockPos home = IHorseData.of(horse).bh_getHome();
        return home != null && horse.distanceToSqr(Vec3.atBottomCenterOf(home)) > NATURAL_WALK_DIST_SQ;
    }

    private void refreshChunkTicket() {
        if (!(horse.level() instanceof ServerLevel serverLevel)) return;
        ticketChunk = horse.chunkPosition();
        ticketRefreshCooldown = TICKET_REFRESH_INTERVAL_TICKS;
        serverLevel.getChunkSource().addRegionTicket(ModTicketTypes.HORSE_TASK, ticketChunk, TICKET_RADIUS, ticketChunk);
    }

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
        BlockPos home = IHorseData.of(horse).bh_getHome();
        if (home == null) return;
        if (horse.level() instanceof ServerLevel serverLevel) {
            ChunkPos chunk = new ChunkPos(home);
            serverLevel.getChunkSource().addRegionTicket(ModTicketTypes.HORSE_TASK, chunk, 1, chunk);
        }
        if (!icy.betterhorses.net.HorsePlacement.teleport(horse, home)) return;
        IHorseData.of(horse).bh_setCommand(BhContent.COMMAND_STAY.getKey());
    }
}
