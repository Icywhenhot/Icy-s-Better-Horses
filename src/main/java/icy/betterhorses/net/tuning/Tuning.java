package icy.betterhorses.net.tuning;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class Tuning {

    private static final String ROOT = "better_horses";
    private static final String BOND = "bond";
    private static final String BREEDING = "breeding";
    private static final String CLASS = "class/";
    private static final String ABILITY = "ability/";

    private static volatile BondRules bond = BondRules.DEFAULTS;
    private static volatile FoalRules foals = FoalRules.DEFAULTS;
    private static volatile Map<String, String> files = Map.of();
    private static volatile int generation;

    private Tuning() {}

    public static BondRules bond() {
        return bond;
    }

    public static FoalRules foals() {
        return foals;
    }

    public static int generation() {
        return generation;
    }

    public static Map<String, String> files() {
        return files;
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) Tuning::load);
    }

    private static void load(ResourceManager manager) {
        Map<String, String> found = new TreeMap<>();
        collect(manager, CLASS, found);
        collect(manager, ABILITY, found);
        single(manager, BOND, found);
        single(manager, BREEDING, found);
        int warned = apply(found, msg -> IcysBetterHorses.LOGGER.warn("[tuning] {}", msg));
        IcysBetterHorses.LOGGER.info("[tuning] loaded {} files, {} warnings", found.size(), warned);
    }

    private static void collect(ResourceManager manager, String dir, Map<String, String> into) {
        Map<ResourceLocation, Resource> res = manager.listResources(ROOT + "/" + dir.substring(0, dir.length() - 1),
                id -> id.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> e : res.entrySet()) {
            String path = e.getKey().getPath();
            String name = path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length());
            text(e.getValue(), e.getKey()).ifPresent(json -> into.put(dir + name, json));
        }
    }

    private static void single(ResourceManager manager, String name, Map<String, String> into) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, ROOT + "/" + name + ".json");
        manager.getResource(id).flatMap(r -> text(r, id)).ifPresent(json -> into.put(name, json));
    }

    private static Optional<String> text(Resource res, ResourceLocation id) {
        try (Reader reader = res.openAsReader()) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) > 0) {
                sb.append(buf, 0, n);
            }
            return Optional.of(sb.toString());
        } catch (Exception e) {
            IcysBetterHorses.LOGGER.warn("[tuning] could not read {}", id, e);
            return Optional.empty();
        }
    }

    public static synchronized int apply(Map<String, String> incoming, Consumer<String> log) {
        int[] count = {0};
        ArchetypeType.resetAll();
        Tune.resetAll();
        touchHolders();
        BondRules nextBond = BondRules.DEFAULTS;
        FoalRules nextFoals = FoalRules.DEFAULTS;

        for (Map.Entry<String, String> e : new TreeMap<>(incoming).entrySet()) {
            String name = e.getKey();
            Consumer<String> warn = msg -> {
                count[0]++;
                log.accept(name + ": " + msg);
            };
            JsonObject obj = parse(e.getValue(), warn);
            if (obj == null) {
                continue;
            }
            if (name.equals(BOND)) {
                nextBond = BondRules.read(obj, warn);
            } else if (name.equals(BREEDING)) {
                nextFoals = FoalRules.read(obj, warn);
            } else if (name.startsWith(CLASS)) {
                applyClass(name.substring(CLASS.length()), obj, warn);
            } else if (name.startsWith(ABILITY)) {
                Tune.load(name.substring(ABILITY.length()), obj, warn);
            } else {
                warn.accept("not a tuning file");
            }
        }

        bond = nextBond;
        foals = nextFoals;
        files = Map.copyOf(incoming);
        generation++;
        return count[0];
    }

    public static synchronized void reset() {
        ArchetypeType.resetAll();
        Tune.resetAll();
        bond = BondRules.DEFAULTS;
        foals = FoalRules.DEFAULTS;
        files = Map.of();
        generation++;
    }

    private static void applyClass(String name, JsonObject obj, Consumer<String> warn) {
        ArchetypeType arch = BhRegistries.archetypeTypeRegistry()
                .get(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, name));
        if (arch == null) {
            warn.accept("no class called " + name);
            return;
        }
        arch.apply(ClassFile.read(obj, arch.builtIn(), warn));
    }

    static JsonObject parse(String text, Consumer<String> warn) {
        try {
            JsonElement el = JsonParser.parseString(text);
            if (el instanceof JsonObject obj) {
                return obj;
            }
            warn.accept("is not a json object");
        } catch (RuntimeException e) {
            warn.accept("is not valid json: " + e.getMessage());
        }
        return null;
    }

    private static void touchHolders() {
        for (Class<?> c : TuneHolders.ALL) {
            try {
                MethodHandles.lookup().ensureInitialized(c);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("cannot init " + c.getName(), e);
            }
        }
    }
}
