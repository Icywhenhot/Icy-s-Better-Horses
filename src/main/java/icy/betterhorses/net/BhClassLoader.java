package icy.betterhorses.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.Map;

public final class BhClassLoader {

    private static final String DIR = "better_horses/class";
    private static final double BLOCKS_PER_RAW = 43.2D;
    private static final double MAX_BPS = 100.0D;

    private BhClassLoader() {}

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "classes");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        load(manager);
                    }
                });
    }

    private static void load(ResourceManager manager) {
        ArchetypeType.resetSpeeds();
        Map<Identifier, Resource> found = manager.listResources(DIR, id -> id.getPath().endsWith(".json"));
        int n = 0;

        for (Map.Entry<Identifier, Resource> entry : found.entrySet()) {
            Identifier file = entry.getKey();
            String name = file.getPath();
            name = name.substring(name.lastIndexOf('/') + 1, name.length() - ".json".length());
            ArchetypeType arch = BhRegistries.archetypeTypeRegistry()
                    .getValue(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, name));
            if (arch == null) {
                IcysBetterHorses.LOGGER.warn("[classes] {} does not name a class, skipping", file);
                continue;
            }

            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!(parsed instanceof JsonObject root)) {
                    IcysBetterHorses.LOGGER.warn("[classes] {} is not a json object, skipping", file);
                    continue;
                }
                double lo = bps(root, "min_speed", arch.lowSpeed());
                double hi = bps(root, "max_speed", arch.highSpeed());
                if (lo > hi) {
                    IcysBetterHorses.LOGGER.warn("[classes] {} has min_speed above max_speed, skipping", file);
                    continue;
                }
                arch.setSpeed(lo, hi);
                n++;
            } catch (Exception exception) {
                IcysBetterHorses.LOGGER.warn("[classes] could not read {}", file, exception);
            }
        }

        IcysBetterHorses.LOGGER.info("[classes] loaded {} class speed overrides", n);
    }

    private static double bps(JsonObject root, String key, double fallback) {
        if (!root.has(key)) {
            return fallback;
        }
        return Math.clamp(root.get(key).getAsDouble(), 0.0D, MAX_BPS) / BLOCKS_PER_RAW;
    }
}
