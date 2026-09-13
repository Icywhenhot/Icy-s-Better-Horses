package icy.betterhorses.net;

import net.minecraft.util.Mth;

public record BhTuning(
        int bondAmount,
        int bondMinutes,
        int spawnWeight,
        int groupMin,
        int groupMax,
        double spawnFloor) {

    public static BhTuning defaults() {
        return new BhTuning(1, 1, 5, 2, 6, 0.10D);
    }

    public BhTuning clamped() {
        int min = Mth.clamp(groupMin, 1, 32);
        return new BhTuning(
                Mth.clamp(bondAmount, 0, 100),
                Mth.clamp(bondMinutes, 1, 1440),
                Mth.clamp(spawnWeight, 0, 1000),
                min,
                Mth.clamp(groupMax, min, 32),
                Mth.clamp(spawnFloor, 0.0D, 1.0D));
    }

    public int bondIntervalTicks() {
        return bondMinutes * 20 * 60;
    }
}
