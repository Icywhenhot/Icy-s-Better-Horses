package icy.betterhorses.net;

import net.minecraft.world.entity.animal.horse.AbstractHorse;

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
}
