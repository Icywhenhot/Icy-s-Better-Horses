package icy.betterhorses.net.tuning;

import com.google.gson.JsonObject;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.registry.ArchetypeType;

import java.util.function.Consumer;

public final class ClassFile {

    static final double BLOCKS_PER_RAW = 43.2D;
    private static final double MAX_BPS = 100.0D;
    private static final double MAX_HEALTH = 1024.0D;
    private static final double MAX_JUMP_BLOCKS = BhJumpHeight.blocks(3.0D);
    private static final double BIG = 1.0E6D;

    private ClassFile() {}

    public static ArchetypeType.Stats read(JsonObject obj, ArchetypeType.Stats old, Consumer<String> warn) {
        Fields f = new Fields(obj, warn);

        double[] speed = range(f, "speed",
                f.num("min_speed", old.lowSpeed() * BLOCKS_PER_RAW, 0.0D, MAX_BPS) / BLOCKS_PER_RAW,
                f.num("max_speed", old.highSpeed() * BLOCKS_PER_RAW, 0.0D, MAX_BPS) / BLOCKS_PER_RAW,
                old.lowSpeed(), old.highSpeed());
        double[] health = range(f, "health",
                f.num("min_health", old.lowHealth(), 1.0D, MAX_HEALTH),
                f.num("max_health", old.highHealth(), 1.0D, MAX_HEALTH),
                old.lowHealth(), old.highHealth());
        double[] jump = range(f, "jump",
                jump(f, "min_jump", old.lowJump()),
                jump(f, "max_jump", old.highJump()),
                old.lowJump(), old.highJump());

        double[] spook = spook(f, old);
        double[] road = f.tiers("road_bonus", new double[]{old.road0(), old.road1(), old.road2()}, 0.0D, 10.0D);

        ArchetypeType.Stats out = new ArchetypeType.Stats(
                speed[0], speed[1], health[0], health[1], jump[0], jump[1],
                f.num("bash_damage", old.bashDamage(), 0.0D, BIG),
                f.num("bash_knockback", old.bashKnockback(), 0.0D, 100.0D),
                spook[0], spook[1], spook[2],
                f.whole("chest_rows", old.chestRows(), 0, 6),
                f.flag("chest_and_riders", old.chestAndRiders()),
                f.flag("large_cart", old.largeCart()),
                f.flag("steady", old.steady()),
                f.whole("medkit_multiplier", old.medkitMultiplier(), 1, 100),
                f.num("knockback_resistance", old.knockbackResistance(), 0.0D, 1.0D),
                f.whole("heal_interval", old.healInterval(), 0, 72000),
                f.flag("powder_snow", old.powderSnow()),
                road[0], road[1], road[2],
                f.num("fall_damage_waiver", old.fallWaiver(), 0.0D, BIG),
                f.num("step_height", old.stepHeight(), 0.0D, 10.0D),
                f.num("armor_multiplier", old.armor(), -1.0D, 10.0D));
        f.leftovers();
        return out;
    }

    private static double jump(Fields f, String key, double old) {
        if (!f.has(key)) {
            return old;
        }
        double blocks = f.num(key, BhJumpHeight.blocks(old), 0.0D, MAX_JUMP_BLOCKS);
        return BhJumpHeight.strength(blocks);
    }

    private static double[] range(Fields f, String what, double lo, double hi, double oldLo, double oldHi) {
        if (lo > hi) {
            f.warn("min_" + what + " is above max_" + what + ", keeping " + what + " as it was");
            return new double[]{oldLo, oldHi};
        }
        return new double[]{lo, hi};
    }

    private static double[] spook(Fields f, ArchetypeType.Stats old) {
        double[] was = {old.spook0(), old.spook1(), old.spook2()};
        if (!f.has("spook_chance")) {
            return was;
        }
        double[] got = f.tiers("spook_chance", was, 0.0D, 1.0D);
        if (got != was && !f.isList("spook_chance")) {
            return new double[]{got[0], got[0] * 0.7D, got[0] * 0.5D};
        }
        return got;
    }
}
