package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.state.CameraRenderState;
import org.jetbrains.annotations.Nullable;

public final class BhRenderContext {
    private static final ThreadLocal<CameraRenderState> CAMERA = new ThreadLocal<>();
    private static final ThreadLocal<Float> TINT = ThreadLocal.withInitial(() -> 1.0F);
    private static final ThreadLocal<Float> OPACITY = ThreadLocal.withInitial(() -> 1.0F);

    private BhRenderContext() {}

    public static void pushCamera(CameraRenderState camera) {
        CAMERA.set(camera);
    }

    public static void clearCamera() {
        CAMERA.remove();
    }

    public static @Nullable CameraRenderState currentCamera() {
        return CAMERA.get();
    }

    public static void pushOpacity(float opacity) {
        OPACITY.set(opacity);
    }

    public static void pushTint(float tint) {
        TINT.set(tint);
    }

    public static int tint(int color) {
        float tint = TINT.get();
        int red = Math.round(((color >> 16) & 255) * tint);
        int green = Math.round(((color >> 8) & 255) * tint);
        int blue = Math.round((color & 255) * tint);
        return (color & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }

    public static void clearOpacity() {
        TINT.remove();
        OPACITY.remove();
    }

    public static float currentOpacity() {
        return OPACITY.get();
    }
}
