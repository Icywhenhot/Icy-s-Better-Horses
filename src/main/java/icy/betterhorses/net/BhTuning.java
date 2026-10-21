package icy.betterhorses.net;

import net.minecraft.util.Mth;

public record BhTuning(
        int bondAmount,
        int bondMinutes,
        int spawnWeight,
        int groupMin,
        int groupMax,
        double spawnFloor,
        int reviveLevels,
        int reviveBondLoss,
        int whistleRange,
        Dismount dismount) {

    public enum Dismount { WANDER, FOLLOW, STAY }

    public static final int WHISTLE_ANYWHERE = 0;
    public static final int WHISTLE_MIN = 16;

    public static BhTuning defaults() {
        return new BhTuning(1, 1, 5, 2, 6, 0.10D, 10, 20, WHISTLE_ANYWHERE, Dismount.WANDER);
    }

    public BhTuning clamped() {
        int min = Mth.clamp(groupMin, 1, 32);
        return new BhTuning(
                Mth.clamp(bondAmount, 0, 100),
                Mth.clamp(bondMinutes, 1, 1440),
                Mth.clamp(spawnWeight, 0, 1000),
                min,
                Mth.clamp(groupMax, min, 32),
                Mth.clamp(spawnFloor, 0.0D, 1.0D),
                Mth.clamp(reviveLevels, 0, 1000),
                Mth.clamp(reviveBondLoss, 0, 100),
                whistleRange <= 0 ? WHISTLE_ANYWHERE : Math.max(whistleRange, WHISTLE_MIN),
                dismount == null ? Dismount.WANDER : dismount);
    }

    public boolean whistleReaches(double distSq) {
        return whistleRange == WHISTLE_ANYWHERE || distSq <= (double) whistleRange * whistleRange;
    }

    public int bondIntervalTicks() {
        return bondMinutes * 20 * 60;
    }
}
