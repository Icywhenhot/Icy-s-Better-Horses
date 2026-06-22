package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.state.CameraRenderState;
import org.jetbrains.annotations.Nullable;

// Per-thread holder for the active CameraRenderState. The vanilla RenderLayer.submit(...) signature in 1.21.11 doesn't carry the camera state, but the enclosing LivingEntityRenderer.submit(...) does. LivingEntityRendererSubmitMixin pushes the value here at HEAD and clears it at TAIL, so any layer running inside that scope (notably HorseStabilizerLayer) can grab it for GeckoLib's GeoObjectRenderer.submit call. Render runs on a single thread, but using ThreadLocal keeps us safe against future threading and against Fabric Loader's loading thread that triggers static init.
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
