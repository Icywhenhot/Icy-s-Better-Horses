package icy.betterhorses.net;

public final class BhGears {

    private static final float[] SPEEDS = {0.0F, 0.20F, 0.45F, 0.70F, 1.00F};

    public static final int TOP_GEAR = SPEEDS.length - 1;

    public static final int TOLT_LOW_GEAR = 2;
    public static final int TOLT_HIGH_GEAR = 3;

    private BhGears() {}

    public static float speed(int gear) {
        return gear > 0 && gear <= TOP_GEAR ? SPEEDS[gear] : 0.0F;
    }

    public static int next(int gear) {
        return gear >= TOP_GEAR || gear < 0 ? 0 : gear + 1;
    }
}
