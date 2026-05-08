package icy.betterhorses.net.client.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * Per-thread holder for the active {@link CameraRenderState}. The vanilla
 * {@code RenderLayer.submit(...)} signature in 1.21.11 doesn't carry the camera state, but the
 * enclosing {@code LivingEntityRenderer.submit(...)} does. {@code LivingEntityRendererSubmitMixin}
 * pushes the value here at HEAD and clears it at TAIL, so any layer running inside that scope
 * (notably {@link HorseStabilizerLayer}) can grab it for GeckoLib's
 * {@code GeoObjectRenderer.performRenderPass} call.
 *
 * Render runs on a single thread, but using {@link ThreadLocal} keeps us safe against future
 * threading and against Fabric Loader's loading thread that triggers static init.
 */
public final class BhRenderContext {
    private static final ThreadLocal<CameraRenderState> CAMERA = new ThreadLocal<>();

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
}
