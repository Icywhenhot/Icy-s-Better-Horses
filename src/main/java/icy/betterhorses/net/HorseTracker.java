package icy.betterhorses.net;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Server-side registry of all loaded, owned horses, backed by HorseTrackerState for the bits that
// must survive chunk unloads and restarts.
//
// Parallel-ticking mods (async, worldthreader) run entity ticks — and therefore mount/dismount,
// taming, and entity load/unload callbacks that reach this class — on worker threads rather than the
// server main thread. Two consequences that this class must respect:
//   1. Never call MinecraftServer#overworld() off-thread: worldthreader guards it with an exclusive
//      world-access lock, so an async worker calling it deadlocks against the world thread that would
//      release it. We resolve the SavedData once at attach() and cache it instead.
//   2. The shared collections are mutated concurrently, so they must be thread-safe.
public final class HorseTracker {

    private static final Map<UUID, AbstractHorse> ownedHorses = new ConcurrentHashMap<>();
    // Resolved once on the main thread at attach(); reused everywhere so no hot path calls overworld().
    private static @Nullable HorseTrackerState cachedState;

    private HorseTracker() {}

    public static void attach(MinecraftServer runningServer) {
        cachedState = HorseTrackerState.get(runningServer);
        IcysBetterHorses.LOGGER.info("[whistle] tracker attached: {}", cachedState.describe());
    }

    // In-memory state must not leak into the next world when a singleplayer world is switched.
    public static void detach() {
        cachedState = null;
        ownedHorses.clear();
    }

    private static @Nullable HorseTrackerState state() {
        return cachedState;
    }

    /** True for a leftover copy of a horse that has since been respawned elsewhere by the whistle. */
    public static boolean isStale(AbstractHorse horse) {
        HorseTrackerState state = state();
        return state != null
                && ((IHorseData) horse).bh_getGeneration() < state.getGeneration(horse.getUUID());
    }

    public static void register(AbstractHorse horse) {
        if (isStale(horse)) return;
        ownedHorses.put(horse.getUUID(), horse);
        HorseTrackerState state = state();
        if (state != null && ((IHorseData) horse).bh_isOwned()) {
            state.recordHorse(horse);
        }
    }

    public static void unregister(AbstractHorse horse) {
        // Two-arg remove: a stale copy being discarded must not evict the live horse's entry.
        ownedHorses.remove(horse.getUUID(), horse);
        HorseTrackerState state = state();
        if (state == null || isStale(horse)) return;

        // A plain chunk unload reports removalReason == null and isAlive() == true — the horse is
        // NOT gone, it just left loaded memory. Only an explicit destroy (killed/discarded) means it
        // can never come back. Crucially, a chunk unload must NEVER delete the respawn snapshot:
        // parallel-ticking mods (async/worldthreader) can momentarily read the owner as null here
        // (the load ran on another thread), and forgetting the snapshot on that misread is exactly
        // what left owned horses un-whistleable after a restart. So on a plain unload we only ever
        // refresh the snapshot for owned horses; we never forget. Explicit disown goes through
        // disown() instead.
        Entity.RemovalReason reason = horse.getRemovalReason();
        boolean destroyed = (reason != null && reason.shouldDestroy()) || !horse.isAlive();
        if (destroyed) {
            // Died or was discarded — no longer respawnable by the whistle.
            state.forgetHorse(horse.getUUID());
            IcysBetterHorses.LOGGER.info("[whistle] forgot horse {} (destroyed, removalReason={})",
                    horse.getUUID(), reason);
        } else if (((IHorseData) horse).bh_isOwned()) {
            state.recordHorse(horse);
            IcysBetterHorses.LOGGER.info("[whistle] snapshot recorded for horse {} at {} (unloaded)",
                    horse.getUUID(), horse.blockPosition());
        }
    }

    /**
     * Explicit disown: the player gave the horse up, so drop it and its respawn snapshot. This is the
     * only path that forgets an owned, living horse — a plain chunk unload never does, so a horse that
     * merely wandered out of loaded chunks stays whistleable (see {@link #unregister}).
     */
    public static void disown(AbstractHorse horse) {
        ownedHorses.remove(horse.getUUID(), horse);
        HorseTrackerState state = state();
        if (state == null) return;
        state.forgetHorse(horse.getUUID());
        IcysBetterHorses.LOGGER.info("[whistle] disowned horse {}", horse.getUUID());
    }

    /** Drops a stored horse's snapshot without touching a live entity — used when disowning an unloaded horse. */
    public static void forgetStoredHorse(UUID horseId) {
        HorseTrackerState state = state();
        if (state != null) {
            state.forgetHorse(horseId);
        }
    }

    public static Collection<AbstractHorse> getAll() {
        return ownedHorses.values();
    }

    public static @Nullable AbstractHorse getLoaded(UUID horseId) {
        return ownedHorses.get(horseId);
    }

    public static void setLastRidden(UUID playerId, AbstractHorse horse) {
        HorseTrackerState state = state();
        if (state != null) {
            state.setLastRidden(playerId, horse.getUUID());
            IcysBetterHorses.LOGGER.info("[whistle] last ridden horse of {} is now {}", playerId, horse.getUUID());
        } else {
            IcysBetterHorses.LOGGER.warn("[whistle] setLastRidden with no attached server — ride not recorded");
        }
    }

    public static @Nullable AbstractHorse getLastRidden(UUID playerId) {
        UUID horseId = getLastRiddenId(playerId);
        return horseId == null ? null : ownedHorses.get(horseId);
    }

    public static @Nullable UUID getLastRiddenId(UUID playerId) {
        HorseTrackerState state = state();
        return state == null ? null : state.getLastRiddenId(playerId);
    }

    public static @Nullable HorseTrackerState.KnownPosition getLastKnownPosition(UUID horseId) {
        HorseTrackerState state = state();
        return state == null ? null : state.getLastKnownPosition(horseId);
    }

    public static @Nullable CompoundTag getSnapshot(UUID horseId) {
        HorseTrackerState state = state();
        return state == null ? null : state.getSnapshot(horseId);
    }

    public static @Nullable UUID findStoredHorseOwnedBy(UUID playerId) {
        HorseTrackerState state = state();
        return state == null ? null : state.findStoredHorseOwnedBy(playerId);
    }

    public static List<UUID> findAllStoredHorsesOwnedBy(UUID playerId) {
        HorseTrackerState state = state();
        return state == null ? List.of() : state.findAllStoredHorsesOwnedBy(playerId);
    }

    public static void setActiveHorse(UUID playerId, UUID horseId) {
        HorseTrackerState state = state();
        if (state != null) {
            state.setActiveHorse(playerId, horseId);
        }
    }

    public static @Nullable UUID getActiveHorseId(UUID playerId) {
        HorseTrackerState state = state();
        return state == null ? null : state.getActiveHorseId(playerId);
    }

    public static void clearActiveHorse(UUID horseId) {
        HorseTrackerState state = state();
        if (state != null) {
            state.clearActiveHorse(horseId);
        }
    }

    /** Queues a horse that was disowned while unloaded; it is released when its chunk next loads. */
    public static void markPendingDisown(UUID horseId) {
        HorseTrackerState state = state();
        if (state != null) {
            state.markPendingDisown(horseId);
        }
    }

    public static boolean consumePendingDisown(UUID horseId) {
        HorseTrackerState state = state();
        return state != null && state.consumePendingDisown(horseId);
    }

    public static int getGeneration(UUID horseId) {
        HorseTrackerState state = state();
        return state == null ? 0 : state.getGeneration(horseId);
    }

    public static void setGeneration(UUID horseId, int generation) {
        HorseTrackerState state = state();
        if (state != null) {
            state.setGeneration(horseId, generation);
        }
    }

    // Called periodically so a crash (which skips unload events) still leaves reasonably fresh snapshots on disk.
    public static void recordLoadedPositions() {
        HorseTrackerState state = state();
        if (state == null) return;
        for (AbstractHorse horse : ownedHorses.values()) {
            state.recordHorse(horse);
        }
    }
}
