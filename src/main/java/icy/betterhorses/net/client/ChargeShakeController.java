package icy.betterhorses.net.client;

public final class ChargeShakeController {

    private static final long DURATION_NS = 250_000_000L;
    private static final float MAGNITUDE = 2.2F;

    private static long start = -1L;

    private ChargeShakeController() {}

    public static void trigger() {
        start = System.nanoTime();
    }

    public static float pitchOffset() {
        float decay = decay();
        return decay <= 0.0F ? 0.0F : (float) Math.sin(decay * 80.0F) * MAGNITUDE * decay;
    }

    public static float yawOffset() {
        float decay = decay();
        return decay <= 0.0F ? 0.0F : (float) Math.cos(decay * 63.0F) * MAGNITUDE * decay;
    }

    private static float decay() {
        if (start < 0L) {
            return 0.0F;
        }
        long elapsed = System.nanoTime() - start;
        if (elapsed >= DURATION_NS) {
            return 0.0F;
        }
        return 1.0F - (float) elapsed / DURATION_NS;
    }

    static {
        BhClientCaches.register(() -> start = -1L);
    }
}
