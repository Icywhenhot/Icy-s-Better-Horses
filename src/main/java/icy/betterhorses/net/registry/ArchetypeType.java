package icy.betterhorses.net.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class ArchetypeType {

    private final double lowSpeed;
    private final double highSpeed;
    private final double lowHealth;
    private final double highHealth;
    private final double lowJump;
    private final double highJump;
    private final double bashDamage;
    private final double bashKnockback;
    private final double baseSpookChance;
    private final int defaultChestRows;
    private final boolean allowsChestAndRiders;
    private final boolean allowsLargeCart;
    private final boolean suppressesRear;
    private final int medkitMultiplier;
    private final double knockbackResistance;
    private final int passiveHealInterval;
    private final boolean walksOnPowderSnow;
    private final double pathSpeedBonusTier0;
    private final double pathSpeedBonusTier1;
    private final double pathSpeedBonusTier2;
    private final double fallDamageWaiver;
    private final double stepHeight;
    private final List<ResourceKey<AbilityType>> classPerks;

    private ArchetypeType(Builder builder) {
        this.lowSpeed = builder.lowSpeed;
        this.highSpeed = builder.highSpeed;
        this.lowHealth = builder.lowHealth;
        this.highHealth = builder.highHealth;
        this.lowJump = builder.lowJump;
        this.highJump = builder.highJump;
        this.bashDamage = builder.bashDamage;
        this.bashKnockback = builder.bashKnockback;
        this.baseSpookChance = builder.baseSpookChance;
        this.defaultChestRows = builder.defaultChestRows;
        this.allowsChestAndRiders = builder.allowsChestAndRiders;
        this.allowsLargeCart = builder.allowsLargeCart;
        this.suppressesRear = builder.suppressesRear;
        this.medkitMultiplier = builder.medkitMultiplier;
        this.knockbackResistance = builder.knockbackResistance;
        this.passiveHealInterval = builder.passiveHealInterval;
        this.walksOnPowderSnow = builder.walksOnPowderSnow;
        this.pathSpeedBonusTier0 = builder.pathSpeedBonusTier0;
        this.pathSpeedBonusTier1 = builder.pathSpeedBonusTier1;
        this.pathSpeedBonusTier2 = builder.pathSpeedBonusTier2;
        this.fallDamageWaiver = builder.fallDamageWaiver;
        this.stepHeight = builder.stepHeight;
        this.classPerks = List.copyOf(builder.classPerks);
    }

    public double lowSpeed() {
        return lowSpeed;
    }

    public double highSpeed() {
        return highSpeed;
    }

    public double lowHealth() {
        return lowHealth;
    }

    public double highHealth() {
        return highHealth;
    }

    public double lowJump() {
        return lowJump;
    }

    public double highJump() {
        return highJump;
    }

    public double bashDamage() {
        return bashDamage;
    }

    public double kickDamage() {
        return bashDamage;
    }

    public double bashKnockback() {
        return bashKnockback;
    }

    public double baseSpookChance() {
        return baseSpookChance;
    }

    public double spookChance(int tier) {
        double multiplier = tier >= 2 ? 0.5D : tier >= 1 ? 0.7D : 1.0D;
        return baseSpookChance * multiplier;
    }

    public int defaultChestRows() {
        return defaultChestRows;
    }

    public boolean allowsChestAndRiders() {
        return allowsChestAndRiders;
    }

    public boolean allowsLargeCart() {
        return allowsLargeCart;
    }

    public boolean suppressesRear() {
        return suppressesRear;
    }

    public int medkitMultiplier() {
        return medkitMultiplier;
    }

    public double knockbackResistance() {
        return knockbackResistance;
    }

    public int passiveHealInterval() {
        return passiveHealInterval;
    }

    public boolean walksOnPowderSnow() {
        return walksOnPowderSnow;
    }

    public double pathSpeedBonus(int tier) {
        return tier >= 2 ? pathSpeedBonusTier2 : tier >= 1 ? pathSpeedBonusTier1 : pathSpeedBonusTier0;
    }

    public double fallDamageWaiver() {
        return fallDamageWaiver;
    }

    public double stepHeight() {
        return stepHeight;
    }

    public List<ResourceKey<AbilityType>> classPerks() {
        return classPerks;
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

    private static double spread(RandomSource random, double lo, double hi) {
        double t = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0D;
        return lo + t * (hi - lo);
    }

    public static double topSpeed() {
        double best = 0.0D;
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            best = Math.max(best, a.highSpeed);
        }
        return best;
    }

    public static double topJump() {
        double best = 0.0D;
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            best = Math.max(best, a.highJump);
        }
        return best;
    }

    public static double topHealth() {
        double best = 0.0D;
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            best = Math.max(best, a.highHealth);
        }
        return best;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private double lowSpeed;
        private double highSpeed;
        private double lowHealth;
        private double highHealth;
        private double lowJump;
        private double highJump;
        private double bashDamage = 1.0D;
        private double bashKnockback = 1.0D;
        private double baseSpookChance = 0.10D;
        private int defaultChestRows = 3;
        private boolean allowsChestAndRiders = false;
        private boolean allowsLargeCart = false;
        private boolean suppressesRear = false;
        private int medkitMultiplier = 1;
        private double knockbackResistance = 0.0D;
        private int passiveHealInterval = 0;
        private boolean walksOnPowderSnow = false;
        private double pathSpeedBonusTier0 = 0.0D;
        private double pathSpeedBonusTier1 = 0.0D;
        private double pathSpeedBonusTier2 = 0.0D;
        private double fallDamageWaiver = 0.0D;
        private double stepHeight = 1.125D;
        private final List<ResourceKey<AbilityType>> classPerks = new ArrayList<>();

        private Builder() {
        }

        public Builder speed(double low, double high) {
            this.lowSpeed = low;
            this.highSpeed = high;
            return this;
        }

        public Builder health(double low, double high) {
            this.lowHealth = low;
            this.highHealth = high;
            return this;
        }

        public Builder jump(double low, double high) {
            this.lowJump = low;
            this.highJump = high;
            return this;
        }

        public Builder bashDamage(double value) {
            this.bashDamage = value;
            return this;
        }

        public Builder bashKnockback(double value) {
            this.bashKnockback = value;
            return this;
        }

        public Builder baseSpookChance(double value) {
            this.baseSpookChance = value;
            return this;
        }

        public Builder defaultChestRows(int value) {
            this.defaultChestRows = value;
            return this;
        }

        public Builder allowsChestAndRiders(boolean value) {
            this.allowsChestAndRiders = value;
            return this;
        }

        public Builder allowsLargeCart(boolean value) {
            this.allowsLargeCart = value;
            return this;
        }

        public Builder suppressesRear(boolean value) {
            this.suppressesRear = value;
            return this;
        }

        public Builder medkitMultiplier(int value) {
            this.medkitMultiplier = value;
            return this;
        }

        public Builder knockbackResistance(double value) {
            this.knockbackResistance = value;
            return this;
        }

        public Builder passiveHealInterval(int value) {
            this.passiveHealInterval = value;
            return this;
        }

        public Builder walksOnPowderSnow(boolean value) {
            this.walksOnPowderSnow = value;
            return this;
        }

        public Builder pathSpeedBonus(double tier0, double tier1, double tier2) {
            this.pathSpeedBonusTier0 = tier0;
            this.pathSpeedBonusTier1 = tier1;
            this.pathSpeedBonusTier2 = tier2;
            return this;
        }

        public Builder fallDamageWaiver(double value) {
            this.fallDamageWaiver = value;
            return this;
        }

        public Builder stepHeight(double value) {
            this.stepHeight = value;
            return this;
        }

        public Builder ability(ResourceKey<AbilityType> key) {
            this.classPerks.add(key);
            return this;
        }

        public ArchetypeType build() {
            return new ArchetypeType(this);
        }
    }
}
