package icy.betterhorses.net.tuning;

import com.google.gson.JsonObject;
import net.minecraft.util.RandomSource;

import java.util.Locale;
import java.util.function.Consumer;

public record FoalRules(Mode mode,
                        double speedMin, double speedMax,
                        double jumpMin, double jumpMax,
                        double healthMin, double healthMax) {

    public enum Mode { BEST, AVERAGE, RANDOM_PARENT, WORST }

    public enum Stat {
        SPEED(43.2D), JUMP(6.0D), HEALTH(1.0D);

        private final double perRaw;

        Stat(double perRaw) {
            this.perRaw = perRaw;
        }

        public double perRaw() {
            return perRaw;
        }
    }

    public static final FoalRules DEFAULTS = new FoalRules(Mode.BEST, -0.5D, 1.0D, -0.5D, 1.0D, -0.5D, 1.0D);

    private static final double SPAN = 1000.0D;

    public double inherit(double a, double b, Stat stat, RandomSource random) {
        double base = switch (mode) {
            case BEST -> Math.max(a, b);
            case WORST -> Math.min(a, b);
            case AVERAGE -> (a + b) * 0.5D;
            case RANDOM_PARENT -> random.nextBoolean() ? a : b;
        };
        double lo = switch (stat) {
            case SPEED -> speedMin;
            case JUMP -> jumpMin;
            case HEALTH -> healthMin;
        };
        double hi = switch (stat) {
            case SPEED -> speedMax;
            case JUMP -> jumpMax;
            case HEALTH -> healthMax;
        };
        return Math.max(0.0D, base + (lo + random.nextDouble() * (hi - lo)) / stat.perRaw());
    }

    public static FoalRules read(JsonObject obj, Consumer<String> warn) {
        FoalRules d = DEFAULTS;
        Fields f = new Fields(obj, warn);
        Mode mode = d.mode;
        String raw = f.word("inherit", null);
        if (raw != null) {
            try {
                mode = Mode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                f.warn("inherit should be best, average, random_parent or worst, got " + raw);
            }
        }
        double[] speed = f.pair("speed_variance", new double[]{d.speedMin, d.speedMax}, -SPAN, SPAN);
        double[] jump = f.pair("jump_variance", new double[]{d.jumpMin, d.jumpMax}, -SPAN, SPAN);
        double[] health = f.pair("health_variance", new double[]{d.healthMin, d.healthMax}, -SPAN, SPAN);
        f.leftovers();
        return new FoalRules(mode, speed[0], speed[1], jump[0], jump[1], health[0], health[1]);
    }
}
