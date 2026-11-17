package icy.betterhorses.net.tuning;

import com.google.gson.JsonObject;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.registry.ArchetypeType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.function.Consumer;

public record Ranges(double lowSpeed, double highSpeed,
                     double lowHealth, double highHealth,
                     double lowJump, double highJump) {

    public static final Ranges NONE = new Ranges(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

    public static final String[] KEYS = {"min_speed", "max_speed", "min_health", "max_health", "min_jump", "max_jump"};

    public static Ranges read(JsonObject obj, Consumer<String> warn) {
        Fields f = new Fields(obj, warn);
        double ls = f.num("min_speed", Double.NaN, 0.0D, 100.0D) / ClassFile.BLOCKS_PER_RAW;
        double hs = f.num("max_speed", Double.NaN, 0.0D, 100.0D) / ClassFile.BLOCKS_PER_RAW;
        double lh = f.num("min_health", Double.NaN, 1.0D, 1024.0D);
        double hh = f.num("max_health", Double.NaN, 1.0D, 1024.0D);
        double lj = jump(f, "min_jump");
        double hj = jump(f, "max_jump");
        Ranges r = new Ranges(ls, hs, lh, hh, lj, hj);
        if (ls > hs) {
            warn.accept("min_speed is above max_speed, using the class speed");
            r = new Ranges(Double.NaN, Double.NaN, lh, hh, lj, hj);
        }
        if (lh > hh) {
            warn.accept("min_health is above max_health, using the class health");
            r = new Ranges(r.lowSpeed, r.highSpeed, Double.NaN, Double.NaN, lj, hj);
        }
        if (lj > hj) {
            warn.accept("min_jump is above max_jump, using the class jump");
            r = new Ranges(r.lowSpeed, r.highSpeed, r.lowHealth, r.highHealth, Double.NaN, Double.NaN);
        }
        return r;
    }

    private static double jump(Fields f, String key) {
        double blocks = f.num(key, Double.NaN, 0.0D, BhJumpHeight.blocks(3.0D));
        return Double.isNaN(blocks) ? Double.NaN : BhJumpHeight.strength(blocks);
    }

    public boolean empty() {
        return equals(NONE);
    }

    public double[] speed(ArchetypeType.Stats cls) {
        return pick(lowSpeed, highSpeed, cls.lowSpeed(), cls.highSpeed());
    }

    public double[] health(ArchetypeType.Stats cls) {
        return pick(lowHealth, highHealth, cls.lowHealth(), cls.highHealth());
    }

    public double[] jump(ArchetypeType.Stats cls) {
        return pick(lowJump, highJump, cls.lowJump(), cls.highJump());
    }

    public double rollSpeed(ArchetypeType.Stats cls, RandomSource random) {
        double[] r = speed(cls);
        return ArchetypeType.spread(random, r[0], r[1]);
    }

    public double rollHealth(ArchetypeType.Stats cls, RandomSource random) {
        double[] r = health(cls);
        return Math.round(ArchetypeType.spread(random, r[0], r[1]));
    }

    public double rollJump(ArchetypeType.Stats cls, RandomSource random) {
        double[] r = jump(cls);
        return ArchetypeType.spread(random, r[0], r[1]);
    }

    public double clampSpeed(ArchetypeType.Stats cls, double v) {
        double[] r = speed(cls);
        return Mth.clamp(v, r[0], r[1]);
    }

    public double clampHealth(ArchetypeType.Stats cls, double v) {
        double[] r = health(cls);
        return Math.round(Mth.clamp(v, r[0], r[1]));
    }

    public double clampJump(ArchetypeType.Stats cls, double v) {
        double[] r = jump(cls);
        return Mth.clamp(v, r[0], r[1]);
    }

    private static double[] pick(double lo, double hi, double clsLo, double clsHi) {
        double a = Double.isNaN(lo) ? clsLo : lo;
        double b = Double.isNaN(hi) ? clsHi : hi;
        return a > b ? new double[]{clsLo, clsHi} : new double[]{a, b};
    }
}
