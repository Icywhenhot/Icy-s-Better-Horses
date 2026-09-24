package icy.betterhorses.net;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.entity.EntityTypeTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BhTwinWatch {

    private static final int SCAN_TICKS = 600;

    private static final Set<String> reported = ConcurrentHashMap.newKeySet();

    private BhTwinWatch() {}

    public static void reset() {
        reported.clear();
    }

    public static void tick(MinecraftServer server, int tick) {
        if (tick % SCAN_TICKS != 0) {
            return;
        }

        Map<String, List<AbstractHorse>> byHorse = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (AbstractHorse horse : level.getEntities(EntityTypeTest.forClass(AbstractHorse.class),
                    candidate -> IHorseData.of(candidate).bh_isOwned())) {
                if (HorseTracker.isStale(horse)) {
                    IcysBetterHorses.LOGGER.info("[twin] discarding stale body missed by the tick sweep: {}",
                            describe(horse));
                    horse.ejectPassengers();
                    horse.discard();
                    continue;
                }
                byHorse.computeIfAbsent(fingerprint(horse), key -> new ArrayList<>()).add(horse);
            }
        }

        for (List<AbstractHorse> bodies : byHorse.values()) {
            if (bodies.size() < 2) {
                continue;
            }
            List<String> ids = new ArrayList<>();
            for (AbstractHorse horse : bodies) {
                ids.add(horse.getStringUUID());
            }
            ids.sort(String::compareTo);
            if (!reported.add(String.join(" ", ids))) {
                continue;
            }

            IHorseData first = IHorseData.of(bodies.get(0));
            IcysBetterHorses.LOGGER.warn("[twin] {} loaded bodies look like one horse: owner {} breed {} bond {} name {}",
                    bodies.size(), first.bh_getOwner(), first.bh_getBreedKey(), first.bh_getBond(),
                    bodies.get(0).hasCustomName() ? bodies.get(0).getCustomName().getString() : "-");
            for (AbstractHorse horse : bodies) {
                IcysBetterHorses.LOGGER.warn("[twin]   {} type {}",
                        describe(horse), EntityType.getKey(horse.getType()));
            }
        }
    }

    public static String describe(AbstractHorse horse) {
        IHorseData data = IHorseData.of(horse);
        UUID id = horse.getUUID();
        return String.format("%s gen %d/%d%s%s %s %d %d %d",
                id,
                data.bh_getGeneration(),
                HorseTracker.getGeneration(id),
                HorseTracker.isStale(horse) ? " STALE" : "",
                HorseTracker.getLoaded(id) == horse ? "" : " untracked",
                horse.level().dimension().identifier(),
                horse.getBlockX(), horse.getBlockY(), horse.getBlockZ());
    }

    public static String fingerprint(AbstractHorse horse) {
        IHorseData data = IHorseData.of(horse);
        return data.bh_getOwner() + "|" + data.bh_getBreedKey() + "|" + data.bh_getBond()
                + "|" + (horse.hasCustomName() ? horse.getCustomName().getString() : "");
    }
}
