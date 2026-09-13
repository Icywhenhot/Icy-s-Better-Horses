package icy.betterhorses.net.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icy.betterhorses.net.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

public final class BhVector {

    private static final double ANGLE_STEP = Math.toRadians(3.0D);
    public static final float FEATHER = 1.0F;

    private BhVector() {}

    public static final class Builder {

        private float[] xy = new float[1024];
        private int[] colors = new int[512];
        private int vertices;

        public void quad(float x0, float y0, int c0, float x1, float y1, int c1,
                         float x2, float y2, int c2, float x3, float y3, int c3) {
            ensure(4);
            vertex(x0, y0, c0);
            vertex(x1, y1, c1);
            vertex(x2, y2, c2);
            vertex(x3, y3, c3);
        }

        private void vertex(float x, float y, int color) {
            xy[vertices * 2] = x;
            xy[vertices * 2 + 1] = y;
            colors[vertices] = color;
            vertices++;
        }

        private void ensure(int more) {
            if (vertices + more <= colors.length) return;
            int capacity = Math.max(colors.length * 2, vertices + more);
            float[] grownXy = new float[capacity * 2];
            int[] grownColors = new int[capacity];
            System.arraycopy(xy, 0, grownXy, 0, vertices * 2);
            System.arraycopy(colors, 0, grownColors, 0, vertices);
            xy = grownXy;
            colors = grownColors;
        }

        public boolean isEmpty() {
            return vertices == 0;
        }
    }

    public static void submit(GuiGraphicsExtractor gfx, Builder builder) {
        if (builder.isEmpty()) return;

        float[] xy = new float[builder.vertices * 2];
        int[] colors = new int[builder.vertices];
        System.arraycopy(builder.xy, 0, xy, 0, xy.length);
        System.arraycopy(builder.colors, 0, colors, 0, colors.length);

        Matrix3x2f pose = new Matrix3x2f(gfx.pose());
        ((GuiGraphicsExtractorAccessor) (Object) gfx).bh_guiRenderState()
                .addGuiElement(new MeshRenderState(xy, colors, pose, bounds(xy, pose)));
    }

    private static ScreenRectangle bounds(float[] xy, Matrix3x2fc pose) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        Vector2f point = new Vector2f();
        for (int i = 0; i < xy.length; i += 2) {
            pose.transformPosition(point.set(xy[i], xy[i + 1]));
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }
        int x = (int) Math.floor(minX);
        int y = (int) Math.floor(minY);
        return new ScreenRectangle(x, y, (int) Math.ceil(maxX) - x + 1, (int) Math.ceil(maxY) - y + 1);
    }

    private record MeshRenderState(float[] xy, int[] colors, Matrix3x2fc pose, ScreenRectangle bounds)
            implements GuiElementRenderState {

        @Override
        public void buildVertices(VertexConsumer consumer) {
            for (int i = 0; i < colors.length; i++) {
                consumer.addVertexWith2DPose(pose, xy[i * 2], xy[i * 2 + 1]).setColor(colors[i]);
            }
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI;
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return null;
        }
    }

    public static void wedge(Builder builder, float cx, float cy, float innerRadius, float outerRadius,
                             double startAngle, double endAngle, int color, float feather) {
        arc(builder, cx, cy, innerRadius, outerRadius, startAngle, endAngle, color, feather, false);
    }

    public static void ring(Builder builder, float cx, float cy, float innerRadius, float outerRadius,
                            int color, float feather) {
        arc(builder, cx, cy, innerRadius, outerRadius, 0.0D, Math.PI * 2.0D, color, feather, true);
    }

    public static void disc(Builder builder, float cx, float cy, float radius, int color, float feather) {
        arc(builder, cx, cy, 0f, radius, 0.0D, Math.PI * 2.0D, color, feather, true);
    }

    private static void arc(Builder builder, float cx, float cy, float innerRadius, float outerRadius,
                            double startAngle, double endAngle, int color, float feather, boolean closed) {
        double span = endAngle - startAngle;
        if (span <= 0.0D || outerRadius <= innerRadius) return;

        float radialFeather = Math.min(feather, (outerRadius - innerRadius) * 0.45F);
        boolean solid = innerRadius <= 0.01F;
        float[] radii = solid
                ? new float[]{0f, outerRadius - radialFeather, outerRadius}
                : new float[]{innerRadius, innerRadius + radialFeather, outerRadius - radialFeather, outerRadius};
        float[] radialAlpha = solid ? new float[]{1f, 1f, 0f} : new float[]{0f, 1f, 1f, 0f};

        double angularFeather = closed ? 0.0D
                : Math.min(span * 0.45D, feather / Math.max(1.0F, (innerRadius + outerRadius) * 0.5F));
        int steps = Math.max(1, (int) Math.ceil(span / ANGLE_STEP));
        double[] angles = new double[steps + 3];
        int count = 0;
        angles[count++] = startAngle;
        if (angularFeather > 0.0D) angles[count++] = startAngle + angularFeather;
        for (int i = 1; i < steps; i++) {
            double a = startAngle + span * i / steps;
            if (a > startAngle + angularFeather && a < endAngle - angularFeather) angles[count++] = a;
        }
        if (angularFeather > 0.0D) angles[count++] = endAngle - angularFeather;
        angles[count++] = endAngle;

        for (int i = 0; i < count - 1; i++) {
            double a0 = angles[i];
            double a1 = angles[i + 1];
            float edge0 = edgeAlpha(a0, startAngle, endAngle, angularFeather);
            float edge1 = edgeAlpha(a1, startAngle, endAngle, angularFeather);
            float cos0 = (float) Math.cos(a0), sin0 = (float) Math.sin(a0);
            float cos1 = (float) Math.cos(a1), sin1 = (float) Math.sin(a1);

            for (int band = 0; band < radii.length - 1; band++) {
                float rNear = radii[band];
                float rFar = radii[band + 1];
                int cNear0 = scaleAlpha(color, radialAlpha[band] * edge0);
                int cFar0 = scaleAlpha(color, radialAlpha[band + 1] * edge0);
                int cFar1 = scaleAlpha(color, radialAlpha[band + 1] * edge1);
                int cNear1 = scaleAlpha(color, radialAlpha[band] * edge1);
                builder.quad(
                        cx + cos0 * rNear, cy + sin0 * rNear, cNear0,
                        cx + cos1 * rNear, cy + sin1 * rNear, cNear1,
                        cx + cos1 * rFar, cy + sin1 * rFar, cFar1,
                        cx + cos0 * rFar, cy + sin0 * rFar, cFar0);
            }
        }
    }

    private static float edgeAlpha(double angle, double startAngle, double endAngle, double feather) {
        if (feather <= 0.0D) return 1f;
        double fromStart = (angle - startAngle) / feather;
        double toEnd = (endAngle - angle) / feather;
        return (float) (BhAnim.clamp01((float) fromStart) * BhAnim.clamp01((float) toEnd));
    }

    public static int scaleAlpha(int argb, float k) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * BhAnim.clamp01(k));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }
}
