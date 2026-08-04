package icy.betterhorses.net.client;

import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Tiny animation helpers shared by the mod's screens: easing curves, an entrance transform
 * (slide-up + scale), a colour-alpha fade, and a spring-like "lift" tracker for hover/selection.
 *
 * <p>Everything is driven by wall-clock time ({@link System#currentTimeMillis()}) so it animates
 * smoothly regardless of the game's tick rate, and it stays framerate-independent by easing with an
 * exponential time-constant rather than a fixed per-frame step.</p>
 */
public final class BhAnim {

    private BhAnim() {}

    public static float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }

    /** Decelerating ease — fast then gentle settle. */
    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    /** Accelerating ease — gentle then fast. Used for exits (slide-down + fade-out). */
    public static float easeInCubic(float t) {
        t = clamp01(t);
        return t * t * t;
    }

    /** Overshoots slightly past 1 then settles — a subtle "pop". */
    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    /** ARGB colour with its alpha channel scaled by {@code a} (0..1). */
    public static int fade(int argb, float a) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * clamp01(a));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    /**
     * Applies the standard panel entrance to the current matrix: the panel starts {@code risePx}
     * below its resting spot and scaled to {@code startScale}, easing to its final place as
     * {@code progress} → 1. Scales about the panel centre so it grows in place.
     */
    public static void enter(Matrix3x2fStack pose, float progress, float centerX, float centerY,
                             float risePx, float startScale) {
        float rise = (1f - progress) * risePx;
        float scale = startScale + (1f - startScale) * progress;
        pose.translate(centerX, centerY + rise);
        pose.scale(scale, scale);
        pose.translate(-centerX, -centerY);
    }

    /**
     * Per-element spring tracker for the hover/selection "lift". Each frame call {@link #beginFrame}
     * once, then {@link #get} per element; the returned value eases toward the target lift and back,
     * giving both the rise on hover and a soft settle on release.
     */
    public static final class Lift {
        private final Map<Object, Float> current = new HashMap<>();
        private long lastMs = -1L;
        private float factor = 1f;

        /** Advances the shared clock once per frame. {@code tauSec} = smoothing time-constant (smaller = snappier). */
        public void beginFrame(float tauSec) {
            long now = System.currentTimeMillis();
            float dt = lastMs < 0L ? 0.016f : Math.min((now - lastMs) / 1000f, 0.05f);
            lastMs = now;
            factor = 1f - (float) Math.exp(-dt / Math.max(1.0e-4f, tauSec));
        }

        /** Current lift (px) for {@code key}, easing toward {@code liftPx} when active else back to 0. */
        public float get(Object key, boolean active, float liftPx) {
            float target = active ? liftPx : 0f;
            float c = current.getOrDefault(key, 0f);
            c += (target - c) * factor;
            if (!active && c < 0.03f) {
                current.remove(key);
                return 0f;
            }
            current.put(key, c);
            return c;
        }

        /** Drops keys not touched since the given set — optional housekeeping; the get() itself self-prunes. */
        public void retainRemovingIdle() {
            Iterator<Map.Entry<Object, Float>> it = current.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue() < 0.03f) {
                    it.remove();
                }
            }
        }
    }

    /**
     * One-shot press "squish": {@link #hit} on click records the time; {@link #scale} returns a scale
     * that dips to {@code 1 - depth} on press and springs back to 1 (with a hair of overshoot) over
     * {@code ms}, then reports 1 forever after (and forgets the key).
     */
    public static final class Press {
        private final Map<Object, Long> hitAt = new HashMap<>();

        public void hit(Object key) {
            hitAt.put(key, System.currentTimeMillis());
        }

        public float scale(Object key, float depth, float ms) {
            Long t0 = hitAt.get(key);
            if (t0 == null) {
                return 1f;
            }
            float t = (System.currentTimeMillis() - t0) / ms;
            if (t >= 1f) {
                hitAt.remove(key);
                return 1f;
            }
            return 1f - depth * (1f - easeOutBack(t));
        }
    }
}
