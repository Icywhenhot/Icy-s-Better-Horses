package icy.betterhorses.net.tuning;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

final class Fields {

    private final JsonObject obj;
    private final Consumer<String> warn;
    private final Set<String> seen = new HashSet<>();

    Fields(JsonObject obj, Consumer<String> warn) {
        this.obj = obj;
        this.warn = warn;
    }

    boolean has(String key) {
        seen.add(key);
        return obj.has(key);
    }

    boolean isList(String key) {
        return obj.get(key) instanceof JsonArray;
    }

    void warn(String msg) {
        warn.accept(msg);
    }

    double num(String key, double fallback, double lo, double hi) {
        if (!has(key)) {
            return fallback;
        }
        Double v = number(key, obj.get(key));
        return v == null ? fallback : clamp(key, v, lo, hi);
    }

    int whole(String key, int fallback, int lo, int hi) {
        if (!has(key)) {
            return fallback;
        }
        Double v = number(key, obj.get(key));
        if (v == null) {
            return fallback;
        }
        long n = Math.round(v);
        if (n != v) {
            warn.accept(key + " should be a whole number, rounded " + v + " to " + n);
        }
        return (int) clamp(key, n, lo, hi);
    }

    boolean flag(String key, boolean fallback) {
        if (!has(key)) {
            return fallback;
        }
        JsonElement e = obj.get(key);
        if (e instanceof JsonPrimitive p && p.isBoolean()) {
            return p.getAsBoolean();
        }
        warn.accept(key + " should be true or false, got " + e);
        return fallback;
    }

    String word(String key, String fallback) {
        if (!has(key)) {
            return fallback;
        }
        JsonElement e = obj.get(key);
        if (e instanceof JsonPrimitive p && p.isString()) {
            return p.getAsString();
        }
        warn.accept(key + " should be a string, got " + e);
        return fallback;
    }

    double[] tiers(String key, double[] fallback, double lo, double hi) {
        if (!has(key)) {
            return fallback;
        }
        return tiers(key, obj.get(key), fallback, lo, hi, warn);
    }

    double[] pair(String key, double[] fallback, double lo, double hi) {
        if (!has(key)) {
            return fallback;
        }
        JsonElement e = obj.get(key);
        if (!(e instanceof JsonArray arr) || arr.size() != 2) {
            warn.accept(key + " should be [min, max], got " + e);
            return fallback;
        }
        Double a = number(key, arr.get(0));
        Double b = number(key, arr.get(1));
        if (a == null || b == null) {
            return fallback;
        }
        double min = clamp(key, a, lo, hi);
        double max = clamp(key, b, lo, hi);
        if (min > max) {
            warn.accept(key + " has min above max, ignored");
            return fallback;
        }
        return new double[]{min, max};
    }

    void leftovers() {
        for (String key : obj.keySet()) {
            if (!seen.contains(key)) {
                warn.accept("unknown key " + key);
            }
        }
    }

    static double[] tiers(String key, JsonElement e, double[] fallback, double lo, double hi, Consumer<String> warn) {
        if (e instanceof JsonArray arr) {
            if (arr.isEmpty()) {
                warn.accept(key + " is an empty list, ignored");
                return fallback;
            }
            if (arr.size() > 3) {
                warn.accept(key + " has more than 3 tiers, extra values ignored");
            }
            double[] out = new double[3];
            for (int i = 0; i < 3; i++) {
                if (i >= arr.size()) {
                    out[i] = out[i - 1];
                    continue;
                }
                Double v = number(key, arr.get(i), warn);
                if (v == null) {
                    return fallback;
                }
                out[i] = clamp(key, v, lo, hi, warn);
            }
            return out;
        }
        Double v = number(key, e, warn);
        if (v == null) {
            return fallback;
        }
        double c = clamp(key, v, lo, hi, warn);
        return new double[]{c, c, c};
    }

    private Double number(String key, JsonElement e) {
        return number(key, e, warn);
    }

    private double clamp(String key, double v, double lo, double hi) {
        return clamp(key, v, lo, hi, warn);
    }

    static Double number(String key, JsonElement e, Consumer<String> warn) {
        if (!(e instanceof JsonPrimitive p) || !p.isNumber()) {
            warn.accept(key + " should be a number, got " + e);
            return null;
        }
        double v = p.getAsDouble();
        if (!Double.isFinite(v)) {
            warn.accept(key + " is not a finite number, ignored");
            return null;
        }
        return v;
    }

    static double clamp(String key, double v, double lo, double hi, Consumer<String> warn) {
        if (v < lo || v > hi) {
            double c = Math.max(lo, Math.min(hi, v));
            warn.accept(key + " " + v + " is outside " + lo + ".." + hi + ", using " + c);
            return c;
        }
        return v;
    }
}
