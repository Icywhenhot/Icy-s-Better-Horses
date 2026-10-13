package icy.betterhorses.net;

public final class BhJumpHeight {

    private static final double GRAVITY = 0.08D;
    private static final double DRAG = 0.98D;

    private BhJumpHeight() {}

    public static double blocks(double strength) {
        double h = 0.0D;
        for (double v = strength; v > 0.0D; v = (v - GRAVITY) * DRAG) {
            h += v;
        }
        return h;
    }

    public static double strength(double blocks) {
        double lo = 0.0D;
        double hi = 3.0D;
        for (int i = 0; i < 60; i++) {
            double mid = (lo + hi) * 0.5D;
            if (blocks(mid) < blocks) lo = mid;
            else hi = mid;
        }
        return (lo + hi) * 0.5D;
    }
}
