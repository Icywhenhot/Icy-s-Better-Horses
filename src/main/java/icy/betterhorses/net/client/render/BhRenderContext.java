package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.Nullable;

public final class BhRenderContext {
    private static final ThreadLocal<CameraRenderState> CAMERA = new ThreadLocal<>();
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

    public static void clearOpacity() {
        OPACITY.remove();
    }

    public static float currentOpacity() {
        return OPACITY.get();
    }
}
