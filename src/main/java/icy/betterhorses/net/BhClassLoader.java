package icy.betterhorses.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.Map;

public final class BhClassLoader {

    private static final String DIR = "better_horses/class";
    private static final double BLOCKS_PER_RAW = 43.2D;
    private static final double MAX_BPS = 100.0D;

    private BhClassLoader() {}

    public static void register(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) BhClassLoader::load);
    }

    private static void load(ResourceManager manager) {
        ArchetypeType.resetSpeeds();
        Map<ResourceLocation, Resource> found = manager.listResources(DIR, id -> id.getPath().endsWith(".json"));
        int n = 0;

        for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            ResourceLocation file = entry.getKey();
            String name = file.getPath();
            name = name.substring(name.lastIndexOf('/') + 1, name.length() - ".json".length());
            ArchetypeType arch = BhRegistries.archetypeTypeRegistry()
                    .getValue(new ResourceLocation(IcysBetterHorses.MOD_ID, name));
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
        return Mth.clamp(root.get(key).getAsDouble(), 0.0D, MAX_BPS) / BLOCKS_PER_RAW;
    }
}
