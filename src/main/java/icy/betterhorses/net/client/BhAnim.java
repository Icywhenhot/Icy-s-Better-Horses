package icy.betterhorses.net.client;

import org.joml.Matrix3x2fStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class BhAnim {

    private BhAnim() {}

    public static float clamp01(float t) {
        return t < 0f ? 0f : (t > 1f ? 1f : t);
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    public static float easeInCubic(float t) {
        t = clamp01(t);
        return t * t * t;
    }

    public static float easeOutBack(float t) {
        t = clamp01(t);
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    public static int fade(int argb, float a) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * clamp01(a));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    public static void enter(Matrix3x2fStack pose, float progress, float centerX, float centerY,
                             float risePx, float startScale) {
        float rise = (1f - progress) * risePx;
        float scale = startScale + (1f - startScale) * progress;
        pose.translate(centerX, centerY + rise);
        pose.scale(scale, scale);
        pose.translate(-centerX, -centerY);
    }

    public static final class Lift {
        private final Map<Object, Float> current = new HashMap<>();
        private long lastMs = -1L;
        private float factor = 1f;

        public void beginFrame(float tauSec) {
            long now = System.currentTimeMillis();
            float dt = lastMs < 0L ? 0.016f : Math.min((now - lastMs) / 1000f, 0.05f);
            lastMs = now;
            factor = 1f - (float) Math.exp(-dt / Math.max(1.0e-4f, tauSec));
        }

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

        public void retainRemovingIdle() {
            Iterator<Map.Entry<Object, Float>> it = current.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue() < 0.03f) {
                    it.remove();
                }
            }
        }
    }

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
