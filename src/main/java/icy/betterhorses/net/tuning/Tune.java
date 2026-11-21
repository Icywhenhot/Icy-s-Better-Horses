package icy.betterhorses.net.tuning;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class Tune {

    private static final Map<String, Map<String, Tune>> GROUPS = new LinkedHashMap<>();

    private final String group;
    private final String key;
    private final double[] defaults;
    private double[] live;
    private double lo = -Double.MAX_VALUE;
    private double hi = Double.MAX_VALUE;

    private Tune(String group, String key, double[] defaults) {
        this.group = group;
        this.key = key;
        this.defaults = defaults;
        this.live = defaults;
    }

    public static synchronized Tune of(String group, String key, double... tiers) {
        if (tiers.length < 1 || tiers.length > 3) {
            throw new IllegalArgumentException("tune " + group + "/" + key + " needs 1 to 3 values");
        }
        Map<String, Tune> keys = GROUPS.computeIfAbsent(group, g -> new LinkedHashMap<>());
        if (keys.containsKey(key)) {
            throw new IllegalStateException("tune " + group + "/" + key + " declared twice");
        }
        double[] full = new double[3];
        for (int i = 0; i < 3; i++) {
            full[i] = tiers[Math.min(i, tiers.length - 1)];
        }
        Tune t = new Tune(group, key, full);
        keys.put(key, t);
        return t;
    }

    public Tune within(double lo, double hi) {
        this.lo = lo;
        this.hi = hi;
        return this;
    }

    public double get() {
        return live[0];
    }

    public double at(int tier) {
        return live[Math.max(0, Math.min(2, tier))];
    }

    public int ticks() {
        return ticks(0);
    }

    public int ticks(int tier) {
        return (int) Math.round(at(tier));
    }

    public float f() {
        return (float) live[0];
    }

    public float f(int tier) {
        return (float) at(tier);
    }

    public String group() {
        return group;
    }

    public String key() {
        return key;
    }

    public double[] defaults() {
        return defaults.clone();
    }

    public static synchronized boolean knows(String group) {
        return GROUPS.containsKey(group);
    }

    public static synchronized Map<String, Map<String, Tune>> all() {
        Map<String, Map<String, Tune>> out = new LinkedHashMap<>();
        GROUPS.forEach((g, keys) -> out.put(g, Collections.unmodifiableMap(new LinkedHashMap<>(keys))));
        return out;
    }

    public static synchronized void resetAll() {
        for (Map<String, Tune> keys : GROUPS.values()) {
            for (Tune t : keys.values()) {
                t.live = t.defaults;
            }
        }
    }

    public static synchronized void load(String group, JsonObject obj, Consumer<String> warn) {
        Map<String, Tune> keys = GROUPS.get(group);
        if (keys == null) {
            warn.accept("no ability called " + group);
            return;
        }
        for (Tune t : keys.values()) {
            t.live = t.defaults;
        }
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            Tune t = keys.get(e.getKey());
            if (t == null) {
                warn.accept("unknown key " + e.getKey());
                continue;
            }
            t.live = Fields.tiers(e.getKey(), e.getValue(), t.defaults, t.lo, t.hi, warn);
        }
    }
}
