package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Robust horse "whistle" recall. A single entry point — {@link #onWhistle(ServerPlayer)} — handles
 * the whole flow on the server thread:
 *
 * <ol>
 *   <li>Always plays the whistle so the player gets feedback even if no horse answers.</li>
 *   <li>Finds the player's most relevant owned horse across <em>every loaded dimension</em>
 *       (last-ridden first, then nearest in the player's dimension, then any other dimension).</li>
 *   <li>Overrides whatever standing order the horse had (Stay / Wander / Return Home / hitched) and
 *       switches it to {@link HorseCommand#FOLLOW}.</li>
 *   <li>If the horse is farther than {@value #TELEPORT_RANGE} blocks (or in another dimension), it
 *       teleports to a safe, non-suffocating spot beside the player; otherwise it walks over via
 *       {@code HorseFollowOwnerGoal}.</li>
 * </ol>
 *
 * <p>The logic mirrors the proven approach of the Callable Horses mod (search all loaded worlds,
 * walk when near and teleport when far) while adding safe landing, cross-dimension teleport, and
 * standing-order override. It deliberately makes no demands on bond level so a freshly tamed horse
 * still answers its owner.
 */
public final class HorseWhistle {

    /** Beyond this many blocks (same dimension), or whenever the horse is in another dimension, it teleports. */
    private static final double TELEPORT_RANGE = 32.0D;
    private static final double TELEPORT_RANGE_SQ = TELEPORT_RANGE * TELEPORT_RANGE;

    /** Horizontal offsets (blocks) searched around the player for a clear landing spot, nearest first. */
    private static final int[][] LANDING_OFFSETS = {
            {0, 0},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2}
    };

    /** Vertical span (blocks) checked below the player's feet when looking for footing. */
    private static final int LANDING_VERTICAL_DROP = 3;

    private HorseWhistle() {}

    public static void onWhistle(ServerPlayer player) {
        playWhistle(player);

        AbstractHorse horse = findHorse(player);
        if (horse == null) {
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.whistle_no_horse"));
            return;
        }

        IHorseData data = (IHorseData) horse;

        // The whistle overrides every standing order — the horse drops Stay/Wander/Return Home and follows.
        data.bh_setCommand(HorseCommand.FOLLOW);

        // A hitched horse would be yanked straight back by its tether tick, so release it first.
        if (data.bh_getHitchpostPos() != null && horse.level() instanceof ServerLevel hitchLevel) {
            HitchpostBlock.releaseHorse(hitchLevel, horse, false);
        }

        // Never yank a horse that's being ridden or is a passenger — just let it follow on foot.
        if (horse.isVehicle() || horse.isPassenger()) {
            return;
        }

        boolean differentDimension = horse.level() != player.level();
        if (differentDimension || horse.distanceToSqr(player) > TELEPORT_RANGE_SQ) {
            teleportToPlayer(horse, player);
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.whistle_summoned"));
        }
    }

    /**
     * Picks the horse the whistle should summon. Preference order: the last horse this player rode
     * (if still loaded, owned, and alive — in any dimension), then the nearest owned horse in the
     * player's own dimension, then any owned horse loaded in another dimension.
     */
    private static @Nullable AbstractHorse findHorse(ServerPlayer player) {
        UUID playerId = player.getUUID();

        AbstractHorse lastRidden = HorseTracker.getLastRidden(playerId);
        if (isRecallable(lastRidden, playerId)) {
            return lastRidden;
        }

        Level playerLevel = player.level();
        AbstractHorse nearestSameDim = null;
        double nearestSameDimSq = Double.MAX_VALUE;
        AbstractHorse anyOtherDim = null;

        for (AbstractHorse candidate : HorseTracker.getAll()) {
            if (!isRecallable(candidate, playerId)) {
                continue;
            }
            if (candidate.level() == playerLevel) {
                double distSq = candidate.distanceToSqr(player);
                if (distSq < nearestSameDimSq) {
                    nearestSameDimSq = distSq;
                    nearestSameDim = candidate;
                }
            } else if (anyOtherDim == null) {
                anyOtherDim = candidate;
            }
        }

        return nearestSameDim != null ? nearestSameDim : anyOtherDim;
    }

    private static boolean isRecallable(@Nullable AbstractHorse horse, UUID playerId) {
        return horse != null
                && horse.isAlive()
                && playerId.equals(((IHorseData) horse).bh_getOwner());
    }

    private static void teleportToPlayer(AbstractHorse horse, ServerPlayer player) {
        ServerLevel destination = (ServerLevel) player.level();
        Vec3 landing = findSafeLanding(destination, horse, player);

        horse.getNavigation().stop();
        horse.setDeltaMovement(Vec3.ZERO);

        // The 8-arg teleport handles both same-dimension repositioning and cross-dimension moves.
        // An empty relative-set means every coordinate is absolute.
        horse.teleportTo(
                destination,
                landing.x, landing.y, landing.z,
                Set.<Relative>of(),
                player.getYRot(), horse.getXRot(),
                false);
    }

    /**
     * Finds a spot beside the player where the horse's bounding box doesn't intersect any blocks,
     * preventing it from teleporting into a wall or suffocating. Tries the player's exact feet first
     * (so the behaviour matches a classic "appear on me" whistle), then nearby horizontal offsets,
     * scanning a few blocks downward at each to settle the horse onto solid footing. Falls back to
     * the player's exact position if nothing clear is found.
     */
    private static Vec3 findSafeLanding(ServerLevel level, AbstractHorse horse, ServerPlayer player) {
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        for (int[] offset : LANDING_OFFSETS) {
            double x = px + offset[0];
            double z = pz + offset[1];
            for (int dy = 0; dy >= -LANDING_VERTICAL_DROP; dy--) {
                double y = py + dy;
                if (isClear(level, horse, x, y, z)) {
                    return new Vec3(x, y, z);
                }
            }
        }

        return new Vec3(px, py, pz);
    }

    private static boolean isClear(ServerLevel level, AbstractHorse horse, double x, double y, double z) {
        AABB box = horse.getDimensions(horse.getPose()).makeBoundingBox(x, y, z);
        return level.noCollision(horse, box);
    }

    private static void playWhistle(ServerPlayer player) {
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModSounds.CALL_WHISTLE,
                SoundSource.PLAYERS,
                1.0F, 1.0F);
    }
}
