package icy.betterhorses.net.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public final class ArchetypeType {

    public record Stats(
            double lowSpeed, double highSpeed,
            double lowHealth, double highHealth,
            double lowJump, double highJump,
            double bashDamage, double bashKnockback,
            double spook0, double spook1, double spook2,
            int chestRows, boolean chestAndRiders, boolean largeCart, boolean steady,
            int medkitMultiplier, double knockbackResistance, int healInterval, boolean powderSnow,
            double road0, double road1, double road2,
            double fallWaiver, double stepHeight, double armor) {
    }

    private final Stats builtIn;
    private Stats live;
    private final List<ResourceKey<AbilityType>> classPerks;

    private ArchetypeType(Builder builder) {
        this.builtIn = builder.stats();
        this.live = builtIn;
        this.classPerks = List.copyOf(builder.classPerks);
    }

    public Stats builtIn() {
        return builtIn;
    }

    public Stats stats() {
        return live;
    }

    public void apply(Stats stats) {
        this.live = stats;
    }

    public static void resetAll() {
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            a.live = a.builtIn;
        }
    }

    public double lowSpeed() {
        return live.lowSpeed();
    }

    public double highSpeed() {
        return live.highSpeed();
    }

    public double lowHealth() {
        return live.lowHealth();
    }

    public double highHealth() {
        return live.highHealth();
    }

    public double lowJump() {
        return live.lowJump();
    }

    public double highJump() {
        return live.highJump();
    }

    public double bashDamage() {
        return live.bashDamage();
    }

    public double kickDamage() {
        return live.bashDamage();
    }

    public double bashKnockback() {
        return live.bashKnockback();
    }

    public double baseSpookChance() {
        return live.spook0();
    }

    public double spookChance(int tier) {
        return tier >= 2 ? live.spook2() : tier >= 1 ? live.spook1() : live.spook0();
    }

    public int defaultChestRows() {
        return live.chestRows();
    }

    public boolean allowsChestAndRiders() {
        return live.chestAndRiders();
    }

    public boolean allowsLargeCart() {
        return live.largeCart();
    }

    public boolean suppressesRear() {
        return live.steady();
    }

    public int medkitMultiplier() {
        return live.medkitMultiplier();
    }

    public double knockbackResistance() {
        return live.knockbackResistance();
    }

    public int passiveHealInterval() {
        return live.healInterval();
    }

    public boolean walksOnPowderSnow() {
        return live.powderSnow();
    }

    public double pathSpeedBonus(int tier) {
        return tier >= 2 ? live.road2() : tier >= 1 ? live.road1() : live.road0();
    }

    public double fallDamageWaiver() {
        return live.fallWaiver();
    }

    public double stepHeight() {
        return live.stepHeight();
    }

    public double armorMultiplier() {
        return live.armor();
    }

    public List<ResourceKey<AbilityType>> classPerks() {
        return classPerks;
    }

    public double rollSpeed(RandomSource random) {
        return spread(random, lowSpeed(), highSpeed());
    }

    public double rollHealth(RandomSource random) {
        return Math.round(spread(random, lowHealth(), highHealth()));
    }

    public double rollJump(RandomSource random) {
        return spread(random, lowJump(), highJump());
    }

    public double clampSpeed(double v) {
        return Mth.clamp(v, lowSpeed(), highSpeed());
    }

    public double clampHealth(double v) {
        return Math.round(Mth.clamp(v, lowHealth(), highHealth()));
    }

    public double clampJump(double v) {
        return Mth.clamp(v, lowJump(), highJump());
    }

    public double midSpeed() {
        return (builtIn.lowSpeed() + builtIn.highSpeed()) * 0.5D;
    }

    public double midHealth() {
        return Math.round((builtIn.lowHealth() + builtIn.highHealth()) * 0.5D);
    }

    public double midJump() {
        return (builtIn.lowJump() + builtIn.highJump()) * 0.5D;
    }

    public static double spread(RandomSource random, double lo, double hi) {
        double t = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0D;
        return lo + t * (hi - lo);
    }

    public static double topSpeed() {
        double best = 0.0D;
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            best = Math.max(best, a.highSpeed());
        }
        return best;
    }

    public static double topJump() {
        double best = 0.0D;
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            best = Math.max(best, a.highJump());
        }
        return best;
    }

    public static double topHealth() {
        double best = 0.0D;
        for (ArchetypeType a : BhRegistries.archetypeTypeRegistry()) {
            best = Math.max(best, a.highHealth());
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
        private double armor = 0.0D;
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

        public Builder armor(double value) {
            this.armor = value;
            return this;
        }

        public Builder ability(ResourceKey<AbilityType> key) {
            this.classPerks.add(key);
            return this;
        }

        public Stats stats() {
            return new Stats(lowSpeed, highSpeed, lowHealth, highHealth, lowJump, highJump,
                    bashDamage, bashKnockback,
                    baseSpookChance, baseSpookChance * 0.7D, baseSpookChance * 0.5D,
                    defaultChestRows, allowsChestAndRiders, allowsLargeCart, suppressesRear,
                    medkitMultiplier, knockbackResistance, passiveHealInterval, walksOnPowderSnow,
                    pathSpeedBonusTier0, pathSpeedBonusTier1, pathSpeedBonusTier2,
                    fallDamageWaiver, stepHeight, armor);
        }

        public ArchetypeType build() {
            return new ArchetypeType(this);
        }
    }
}
