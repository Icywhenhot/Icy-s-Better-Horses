package icy.betterhorses.net;

import net.minecraft.world.entity.animal.equine.AbstractHorse;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side registry of all loaded, owned horses.
 * Populated via entity load/unload events and bh_setOwner hooks.
 * All access is on the server main thread — no synchronization needed.
 */
public final class HorseTracker {

    private static final Map<UUID, AbstractHorse> ownedHorses = new HashMap<>();
    private static final Map<UUID, UUID> lastRiddenByPlayer = new HashMap<>();

    private HorseTracker() {}

    public static void register(AbstractHorse horse) {
        ownedHorses.put(horse.getUUID(), horse);
    }

    public static void unregister(AbstractHorse horse) {
        ownedHorses.remove(horse.getUUID());
    }

    public static Collection<AbstractHorse> getAll() {
        return ownedHorses.values();
    }

    public static @Nullable AbstractHorse getOwned(UUID horseId) {
        return ownedHorses.get(horseId);
    }

    public static void setLastRidden(UUID playerId, AbstractHorse horse) {
        lastRiddenByPlayer.put(playerId, horse.getUUID());
    }

    public static @Nullable AbstractHorse getLastRidden(UUID playerId) {
        UUID horseId = lastRiddenByPlayer.get(playerId);
        return horseId == null ? null : ownedHorses.get(horseId);
    }
}
