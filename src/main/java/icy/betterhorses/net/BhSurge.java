package icy.betterhorses.net;

public final class BhSurge {

    public static final int IDLE = 0;
    public static final int ARMED = 1;
    public static final int ACTIVE = 2;
    public static final int COOLING = 3;
    public static final int PULSE = 4;

    public static final int PULSE_TICKS = 40;
    public static final int HIDDEN = -1;

    private static final int TICK_MAX = 511;
    private static final int PERCENT_MAX = 127;
    private static final int VARIANT_MAX = 7;

    private BhSurge() {}

    public static int pack(int phase, int ticks, int span, int percent) {
        return pack(phase, ticks, span, percent, 0);
    }

    public static int pack(int phase, int ticks, int span, int percent, int variant) {
        int t = Math.clamp(ticks, 0, TICK_MAX);
        int s = Math.clamp(span, 0, TICK_MAX);
        int p = Math.clamp(percent, 0, PERCENT_MAX);
        int v = Math.clamp(variant, 0, VARIANT_MAX);
        return (phase & 7) | (t << 3) | (s << 12) | (p << 21) | (v << 28);
    }

    public static int phase(int packed) {
        return packed & 7;
    }

    public static int ticks(int packed) {
        return (packed >> 3) & TICK_MAX;
    }

    public static int span(int packed) {
        return (packed >> 12) & TICK_MAX;
    }

    public static int percent(int packed) {
        return (packed >> 21) & PERCENT_MAX;
    }

    public static int variant(int packed) {
        return (packed >> 28) & VARIANT_MAX;
    }

    public static float fill(int packed) {
        int span = span(packed);
        return span <= 0 ? 1.0F : Math.clamp((float) ticks(packed) / span, 0.0F, 1.0F);
    }

    public static void pulse(IHorseData data, int percent) {
        pulse(data, percent, 0);
    }

    public static void pulse(IHorseData data, int percent, int variant) {
        data.bh_setPulse(pack(PULSE, PULSE_TICKS, PULSE_TICKS, percent, variant));
    }

    public static void decay(IHorseData data) {
        int packed = data.bh_getPulse();
        if (phase(packed) == PULSE) {
            data.bh_setPulse(step(packed));
        }
    }

    public static void pulsePerk(IHorseData data, int variant) {
        data.bh_setPerkSurge(pack(PULSE, PULSE_TICKS, PULSE_TICKS, 0, variant));
    }

    public static void decayPerk(IHorseData data) {
        int packed = data.bh_getPerkSurge();
        if (phase(packed) != PULSE) {
            return;
        }
        data.bh_setPerkSurge(step(packed));
    }

    private static int step(int packed) {
        int left = ticks(packed) - 1;
        return left <= 0
                ? 0
                : pack(PULSE, left, span(packed), percent(packed), variant(packed));
    }
}
