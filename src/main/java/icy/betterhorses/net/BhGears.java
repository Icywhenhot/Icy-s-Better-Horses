package icy.betterhorses.net;

public final class BhGears {

    private static final float[] SPEEDS = {0.0F, 0.20F, 0.45F, 0.70F, 1.00F};
    private static final float PACE_BASE = 0.225F;

    public static final int TOP_GEAR = SPEEDS.length - 1;

    public static final int WALK_GEAR = 1;
    public static final int TROT_GEAR = 2;
    public static final int CANTER_GEAR = 3;
    public static final int GALLOP_GEAR = 4;

    public static final int TOLT_GEAR = TROT_GEAR;

    private BhGears() {}

    public static float riddenSpeed(int gear, float full) {
        if (gear <= 0) {
            return full;
        }
        if (gear < GALLOP_GEAR) {
            return Math.min(PACE_BASE * SPEEDS[gear], full);
        }
        return full * speed(gear);
    }

    private static float speed(int gear) {
        return gear > 0 && gear <= TOP_GEAR ? SPEEDS[gear] : 0.0F;
    }

    public static int next(int gear) {
        return gear >= TOP_GEAR || gear < 0 ? 0 : gear + 1;
    }
}
