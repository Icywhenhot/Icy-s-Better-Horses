package icy.betterhorses.net.tuning;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

public record BondRules(
        int maxBond, int tierTwo, int tierThree,
        int growthEvery, int growthSteps, double growthSpeed, double growthJump, double growthHealth,
        double rideBlocks, int rideBond,
        int jumpsPerBond, int jumpDailyCap,
        int killBond, int killDailyCap,
        int saveBond, int saveDailyCap,
        int hitLoss,
        int treatBond, int treatCooldown,
        int gappleBond, int gappleCooldown,
        int foalCap) {

    public static final int CEILING = 10000;
    private static final double FLOOR = -0.95D;
    private static final int DAY_TICKS_CAP = 24000 * 365;

    public static final BondRules DEFAULTS = new BondRules(
            100, 40, 100,
            20, 5, 0.15D, 0.15D, 0.0D,
            1000.0D, 1,
            10, 2,
            1, 3,
            2, 2,
            2,
            1, 6000,
            2, 6000,
            50);

    public int tier(int bond) {
        if (bond >= tierThree) return 2;
        if (bond >= tierTwo) return 1;
        return 0;
    }

    public int clamp(int bond) {
        return Math.max(0, Math.min(maxBond, bond));
    }

    public int steps(int bond) {
        return Math.max(0, Math.min(bond / growthEvery, growthSteps));
    }

    public double speedBonus(int bond) {
        return Math.max(FLOOR, steps(bond) * growthSpeed);
    }

    public double jumpBonus(int bond) {
        return Math.max(FLOOR, steps(bond) * growthJump);
    }

    public double healthBonus(int bond) {
        return Math.max(FLOOR, steps(bond) * growthHealth);
    }

    public double topSpeedBonus() {
        return speedBonus(maxBond);
    }

    public double speedScale() {
        return 1.0D + Math.max(0.0D, speedBonus(maxBond));
    }

    public double jumpScale() {
        return 1.0D + Math.max(0.0D, jumpBonus(maxBond));
    }

    public double healthScale() {
        return 1.0D + Math.max(0.0D, healthBonus(maxBond));
    }

    public static BondRules read(JsonObject obj, Consumer<String> warn) {
        BondRules d = DEFAULTS;
        Fields f = new Fields(obj, warn);
        int max = f.whole("max_bond", d.maxBond, 1, CEILING);
        int two = f.whole("tier_two", d.tierTwo, 0, CEILING);
        int three = f.whole("tier_three", d.tierThree, 0, CEILING);
        if (two > three) {
            f.warn("tier_two is above tier_three, keeping both tiers as they were");
            two = d.tierTwo;
            three = d.tierThree;
        }
        if (three > max) {
            f.warn("tier_three " + three + " is above max_bond " + max + ", tier 3 can never be reached");
        } else if (two > max) {
            f.warn("tier_two " + two + " is above max_bond " + max + ", tier 2 can never be reached");
        }
        BondRules out = new BondRules(
                max, two, three,
                f.whole("growth_every", d.growthEvery, 1, CEILING),
                f.whole("growth_steps", d.growthSteps, 0, CEILING),
                f.num("growth_speed", d.growthSpeed, FLOOR, 10.0D),
                f.num("growth_jump", d.growthJump, FLOOR, 10.0D),
                f.num("growth_health", d.growthHealth, FLOOR, 10.0D),
                f.num("ride_blocks", d.rideBlocks, 1.0D, 1.0E7D),
                f.whole("ride_bond", d.rideBond, 0, CEILING),
                f.whole("jumps_per_bond", d.jumpsPerBond, 1, CEILING),
                f.whole("jump_daily_cap", d.jumpDailyCap, 0, CEILING),
                f.whole("kill_bond", d.killBond, 0, CEILING),
                f.whole("kill_daily_cap", d.killDailyCap, 0, CEILING),
                f.whole("save_bond", d.saveBond, 0, CEILING),
                f.whole("save_daily_cap", d.saveDailyCap, 0, CEILING),
                f.whole("hit_loss", d.hitLoss, 0, CEILING),
                f.whole("treat_bond", d.treatBond, 0, CEILING),
                f.whole("treat_cooldown", d.treatCooldown, 0, DAY_TICKS_CAP),
                f.whole("golden_apple_bond", d.gappleBond, 0, CEILING),
                f.whole("golden_apple_cooldown", d.gappleCooldown, 0, DAY_TICKS_CAP),
                f.whole("foal_cap", d.foalCap, 0, CEILING));
        f.leftovers();
        return out;
    }
}
