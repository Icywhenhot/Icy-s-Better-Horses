package icy.betterhorses.net.client;

public final class BhSlotFlash {

    private static final long DURATION_MS = 700L;
    private static final int PULSES = 2;

    private static long startedAt = 0L;
    private static int slot = -1;

    private BhSlotFlash() {}

    public static void trigger(int slotIndex) {
        startedAt = System.currentTimeMillis();
        slot = slotIndex;
    }

    public static int flashingSlot() {
        return slot;
    }

    public static float intensity() {
        if (startedAt == 0L) {
            return 0f;
        }

        float progress = (System.currentTimeMillis() - startedAt) / (float) DURATION_MS;
        if (progress >= 1f) {
            startedAt = 0L;
            slot = -1;
            return 0f;
        }

        float pulse = (float) Math.abs(Math.sin(progress * Math.PI * PULSES));
        return pulse * (1f - progress);
    }
}
