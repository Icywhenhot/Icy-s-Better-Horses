package icy.betterhorses.net.client;

// a one-shot red "no" flash for a GUI slot that just refused a click
public final class BhSlotFlash {

    // how long one refusal flashes
    private static final long DURATION_MS = 700L;
    // number of red pulses across that window
    private static final int PULSES = 2;

    // wall-clock start of the current flash, or 0 when nothing is flashing
    private static long startedAt = 0L;
    // menu slot index being flashed, or -1 for none
    private static int slot = -1;

    private BhSlotFlash() {}

    // starts (or restarts) the flash on one menu slot
    public static void trigger(int slotIndex) {
        startedAt = System.currentTimeMillis();
        slot = slotIndex;
    }

    // which slot is flashing, or -1. only meaningful while intensity() is above zero
    public static int flashingSlot() {
        return slot;
    }

    // this frame's flash strength, 0 (idle) to 1 (peak of a pulse)
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

        // |sin| gives PULSES clean 0→1→0 humps; the linear term damps each one below the last so the flash
        float pulse = (float) Math.abs(Math.sin(progress * Math.PI * PULSES));
        return pulse * (1f - progress);
    }
}
