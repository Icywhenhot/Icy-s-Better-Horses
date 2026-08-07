package icy.betterhorses.net.client;

/**
 * A one-shot red "no" flash for a GUI slot that just refused a click.
 *
 * <p>Only one slot in the mod can be refused at a time (the cart's gear slot, held shut by a chest
 * fitted to the cart), so this is a single static timer rather than a per-slot map. The click that
 * triggers it is cancelled client-side — see {@code AbstractContainerScreenMixin} — so the flash
 * <i>is</i> the feedback: without it a locked slot would simply do nothing when clicked.</p>
 *
 * <p>Wall-clock driven like {@link BhAnim}, so it plays at the same rate regardless of tick rate.</p>
 */
public final class BhSlotFlash {

    /** How long one refusal flashes for. */
    private static final long DURATION_MS = 700L;
    /** Number of red pulses across that window. Two reads as a deliberate "no", one as a glitch. */
    private static final int PULSES = 2;

    /** Wall-clock start of the current flash, or 0 when nothing is flashing. */
    private static long startedAt = 0L;

    private BhSlotFlash() {}

    /** Starts (or restarts) the flash. Called when a refused slot is clicked. */
    public static void trigger() {
        startedAt = System.currentTimeMillis();
    }

    /** This frame's flash strength, 0 (idle) to 1 (peak of a pulse). */
    public static float intensity() {
        if (startedAt == 0L) {
            return 0f;
        }

        float progress = (System.currentTimeMillis() - startedAt) / (float) DURATION_MS;
        if (progress >= 1f) {
            startedAt = 0L;
            return 0f;
        }

        // |sin| gives PULSES clean 0→1→0 humps; the linear term damps each one below the last so the
        // flash dies away instead of stopping mid-pulse.
        float pulse = (float) Math.abs(Math.sin(progress * Math.PI * PULSES));
        return pulse * (1f - progress);
    }
}
