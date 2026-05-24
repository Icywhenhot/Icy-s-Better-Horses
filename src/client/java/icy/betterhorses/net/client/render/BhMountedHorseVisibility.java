package icy.betterhorses.net.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class BhMountedHorseVisibility {
    public static final float HEAD_PITCH_OFFSET = 0.2F;
    public static final float HEAD_Y_OFFSET = 2.0F;

    private static final float START_ANGLE = 30.0F;
    private static final float END_ANGLE = 70.0F;
    private static final float MIN_OPACITY = 0.08F;

    private static final ThreadLocal<Float> CURRENT_OPACITY = ThreadLocal.withInitial(() -> 1.0F);

    public static void pushOpacity(float opacity) {
        CURRENT_OPACITY.set(opacity);
    }

    public static void popOpacity() {
        CURRENT_OPACITY.set(1.0F);
    }

    public static float currentOpacity() {
        return CURRENT_OPACITY.get();
    }

    private BhMountedHorseVisibility() {}

    public static float getOpacity(AbstractHorse horse) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !minecraft.options.getCameraType().isFirstPerson()
                || !horse.hasPassenger(minecraft.player)) {
            return 1.0F;
        }

        float angle = minecraft.player.getXRot();
        if (angle < START_ANGLE) {
            return 1.0F;
        }

        float delta = Mth.clamp((Math.min(angle, END_ANGLE) - START_ANGLE) / (END_ANGLE - START_ANGLE), 0.0F, 1.0F);
        return Mth.lerp(delta, 1.0F, MIN_OPACITY);
    }
}
