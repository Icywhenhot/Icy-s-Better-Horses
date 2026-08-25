package icy.betterhorses.net.client.render;

import java.util.HashMap;
import java.util.Map;

public record BhRiderMotion(float right, float up, float forward, float pitch, float roll) {

    public static final BhRiderMotion NONE = new BhRiderMotion(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    private static final Map<Integer, BhRiderMotion> ACTIVE = new HashMap<>();

    public static void publish(int horseId, BhRiderMotion motion) {
        ACTIVE.put(horseId, motion);
    }

    public static BhRiderMotion get(int horseId) {
        return ACTIVE.getOrDefault(horseId, NONE);
    }

    public static void reset() {
        ACTIVE.clear();
    }

    public boolean isRest() {
        return right == 0.0F && up == 0.0F && forward == 0.0F && pitch == 0.0F && roll == 0.0F;
    }
}
