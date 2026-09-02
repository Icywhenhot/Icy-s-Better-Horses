package icy.betterhorses.net;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public enum BreedArchetype {
    RACE(0.2546D, 0.3472D, 15.0D, 20.0D, 0.72D, 1.02D),
    WAR(0.2083D, 0.3472D, 25.0D, 40.0D, 0.57D, 0.97D),
    WESTERN(0.1852D, 0.3472D, 15.0D, 30.0D, 0.57D, 0.84D),
    DRAFT(0.1389D, 0.1852D, 35.0D, 50.0D, 0.38D, 0.72D),
    PONY(0.1852D, 0.3009D, 25.0D, 40.0D, 0.57D, 0.84D),
    NONE(0.1125D, 0.3375D, 15.0D, 30.0D, 0.40D, 1.00D);

    private final double lowSpeed;
    private final double highSpeed;
    private final double lowHealth;
    private final double highHealth;
    private final double lowJump;
    private final double highJump;

    BreedArchetype(double lowSpeed, double highSpeed,
                   double lowHealth, double highHealth,
                   double lowJump, double highJump) {
        this.lowSpeed = lowSpeed;
        this.highSpeed = highSpeed;
        this.lowHealth = lowHealth;
        this.highHealth = highHealth;
        this.lowJump = lowJump;
        this.highJump = highJump;
    }

    public double rollSpeed(RandomSource random) {
        return spread(random, lowSpeed, highSpeed);
    }

    public double rollHealth(RandomSource random) {
        return Math.round(spread(random, lowHealth, highHealth));
    }

    public double rollJump(RandomSource random) {
        return spread(random, lowJump, highJump);
    }

    public double clampSpeed(double v) {
        return Mth.clamp(v, lowSpeed, highSpeed);
    }

    public double clampHealth(double v) {
        return Math.round(Mth.clamp(v, lowHealth, highHealth));
    }

    public double clampJump(double v) {
        return Mth.clamp(v, lowJump, highJump);
    }

    public double midSpeed() {
        return (lowSpeed + highSpeed) * 0.5D;
    }

    public double midHealth() {
        return Math.round((lowHealth + highHealth) * 0.5D);
    }

    public double midJump() {
        return (lowJump + highJump) * 0.5D;
    }

    public double bashDamage() {
        return switch (this) {
            case RACE -> 0.6D;
            case WAR -> 1.5D;
            case DRAFT -> 1.25D;
            case PONY -> 0.75D;
            default -> 1.0D;
        };
    }

    public double kickDamage() {
        return bashDamage();
    }

    public double bashKnockback() {
        return switch (this) {
            case RACE, PONY -> 0.8D;
            case WAR -> 1.2D;
            case DRAFT -> 2.0D;
            default -> 1.0D;
        };
    }

    public double spookChance(int tier) {
        if (this == WAR || this == NONE) {
            return 0.0D;
        }
        double base = tier >= 2 ? 0.05D : tier >= 1 ? 0.07D : 0.10D;
        return this == DRAFT ? base * 0.5D : base;
    }

    public int chestRows() {
        return this == DRAFT ? 4 : 3;
    }

    public boolean allowsChestAndRiders() {
        return this == DRAFT;
    }

    public boolean suppressRear() {
        return this == WAR;
    }

    public int medkitMultiplier() {
        return this == WAR ? 2 : 1;
    }

    public double knockbackResistance() {
        return this == DRAFT ? 0.6D : 0.0D;
    }

    public int passiveHealInterval() {
        return this == PONY ? 400 : 0;
    }

    public boolean walksOnPowderSnow() {
        return this == PONY;
    }

    public double pathSpeedBonus(int tier) {
        if (this != WESTERN) {
            return 0.0D;
        }
        return tier >= 2 ? 0.50D : tier >= 1 ? 0.20D : 0.10D;
    }

    public double fallDamageWaiver() {
        return this == PONY ? 15.0D : 0.0D;
    }

    public double stepHeight() {
        return this == PONY ? 2.0D : 1.0D;
    }

    public static double topSpeed() {
        double best = 0.0D;
        for (BreedArchetype a : values()) {
            best = Math.max(best, a.highSpeed);
        }
        return best;
    }

    public static double topJump() {
        double best = 0.0D;
        for (BreedArchetype a : values()) {
            best = Math.max(best, a.highJump);
        }
        return best;
    }

    public static double topHealth() {
        double best = 0.0D;
        for (BreedArchetype a : values()) {
            best = Math.max(best, a.highHealth);
        }
        return best;
    }

    private static double spread(RandomSource random, double lo, double hi) {
        double t = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0D;
        return lo + t * (hi - lo);
    }
}
