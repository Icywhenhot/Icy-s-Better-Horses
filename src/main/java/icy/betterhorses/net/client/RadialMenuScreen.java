package icy.betterhorses.net.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class RadialMenuScreen extends Screen {

    private static final int SEGMENT_COUNT = 4;
    private static final int RING_INNER = 42;
    private static final int RING_OUTER = 112;
    private static final int CENTER_RADIUS = 33;
    private static final int LABEL_RADIUS = 79;
    private static final double SEGMENT_GAP_RADIANS = Math.toRadians(3.0D);
    private static final int BASE_BACKGROUND_COLOR = 0x4A080B14;
    private static final int OUTER_HALO_COLOR = 0x3019223A;
    private static final int INNER_DISC_COLOR = 0xDE82A7E8;
    private static final int INNER_DISC_SHADOW_COLOR = 0x8C6385C5;
    private static final int[] SEGMENT_COLORS = {
            0xD06D89C6,
            0xD09AB8F5,
            0xD06B87C0,
            0xD094B3F0
    };
    private static final int HOVERED_SEGMENT_COLOR = 0xF0BAD4FF;
    private static final int SEGMENT_SHADOW_COLOR = 0x50111826;

    private static final HorseCommand[] COMMANDS = {
            HorseCommand.FOLLOW,
            HorseCommand.SET_HOME,
            HorseCommand.STAY,
            HorseCommand.RETURN_HOME,
    };

    private static final int[] LABEL_DX = {0, 1, 0, -1};
    private static final int[] LABEL_DY = {-1, 0, 1, 0};

    private final int horseId;
    private int hoveredIndex = -1;

    public RadialMenuScreen(int horseId) {
        super(Component.translatable("screen.icys-better-horses.radial"));
        this.horseId = horseId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        int cx = width / 2;
        int cy = height / 2;

        double angle = Math.atan2(mouseY - cy, mouseX - cx);
        double dist = Math.sqrt((mouseX - cx) * (double)(mouseX - cx) + (mouseY - cy) * (double)(mouseY - cy));
        hoveredIndex = (dist >= RING_INNER && dist <= RING_OUTER) ? angleToIndex(angle) : -1;

        // Dim background
        gfx.fill(0, 0, width, height, BASE_BACKGROUND_COLOR);

        // Soft backdrop for the radial.
        bh_drawFilledCircle(gfx, cx, cy, RING_OUTER + 8, OUTER_HALO_COLOR);

        // Draw smooth solid segments.
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            int color = (i == hoveredIndex) ? HOVERED_SEGMENT_COLOR : SEGMENT_COLORS[i];
            bh_drawSegmentShadow(gfx, cx, cy, i);
            bh_drawSegment(gfx, cx, cy, i, color);
        }

        bh_drawFilledCircle(gfx, cx, cy, CENTER_RADIUS + 4, INNER_DISC_SHADOW_COLOR);
        bh_drawFilledCircle(gfx, cx, cy, CENTER_RADIUS, INNER_DISC_COLOR);

        // Draw labels
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            int lx = cx + (int)(LABEL_DX[i] * LABEL_RADIUS);
            int ly = cy + (int)(LABEL_DY[i] * LABEL_RADIUS);
            String text = Component.translatable(commandKey(COMMANDS[i])).getString();
            int textColor = (i == hoveredIndex) ? 0xFFFFFFFF : 0xFFCCCCCC;
            gfx.drawCenteredString(font, text, lx, ly - font.lineHeight / 2, textColor);
        }

        // Center dot
        gfx.fill(cx - 4, cy - 4, cx + 4, cy + 4, 0xFF888888);
    }

    private void bh_drawSegment(GuiGraphics gfx, int cx, int cy, int index, int color) {
        double segAngle = Math.PI * 2 / SEGMENT_COUNT;
        double startAngle = segAngle * index - Math.PI / 2 - segAngle / 2 + SEGMENT_GAP_RADIANS;
        double endAngle = startAngle + segAngle - SEGMENT_GAP_RADIANS * 2.0D;
        bh_drawAnnulus(gfx, cx, cy, startAngle, endAngle, RING_INNER, RING_OUTER, color);
    }

    private void bh_drawSegmentShadow(GuiGraphics gfx, int cx, int cy, int index) {
        double segAngle = Math.PI * 2 / SEGMENT_COUNT;
        double startAngle = segAngle * index - Math.PI / 2 - segAngle / 2 + SEGMENT_GAP_RADIANS;
        double endAngle = startAngle + segAngle - SEGMENT_GAP_RADIANS * 2.0D;
        bh_drawAnnulus(gfx, cx + 1, cy + 2, startAngle, endAngle, RING_INNER, RING_OUTER, SEGMENT_SHADOW_COLOR);
    }

    private void bh_drawAnnulus(
            GuiGraphics gfx,
            int cx,
            int cy,
            double startAngle,
            double endAngle,
            int innerRadius,
            int outerRadius,
            int color) {
        Matrix4f matrix = gfx.pose().last().pose();
        int steps = Math.max(24, (int) Math.ceil((endAngle - startAngle) * outerRadius / 10.0D));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int step = 0; step < steps; step++) {
            double angle1 = startAngle + (endAngle - startAngle) * step / steps;
            double angle2 = startAngle + (endAngle - startAngle) * (step + 1) / steps;

            float outerX1 = (float) (cx + Math.cos(angle1) * outerRadius);
            float outerY1 = (float) (cy + Math.sin(angle1) * outerRadius);
            float outerX2 = (float) (cx + Math.cos(angle2) * outerRadius);
            float outerY2 = (float) (cy + Math.sin(angle2) * outerRadius);
            float innerX1 = (float) (cx + Math.cos(angle1) * innerRadius);
            float innerY1 = (float) (cy + Math.sin(angle1) * innerRadius);
            float innerX2 = (float) (cx + Math.cos(angle2) * innerRadius);
            float innerY2 = (float) (cy + Math.sin(angle2) * innerRadius);

            buffer.addVertex(matrix, outerX1, outerY1, 0.0F).setColor(color);
            buffer.addVertex(matrix, outerX2, outerY2, 0.0F).setColor(color);
            buffer.addVertex(matrix, innerX1, innerY1, 0.0F).setColor(color);

            buffer.addVertex(matrix, innerX1, innerY1, 0.0F).setColor(color);
            buffer.addVertex(matrix, outerX2, outerY2, 0.0F).setColor(color);
            buffer.addVertex(matrix, innerX2, innerY2, 0.0F).setColor(color);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private void bh_drawFilledCircle(GuiGraphics gfx, int cx, int cy, int radius, int color) {
        Matrix4f matrix = gfx.pose().last().pose();
        int steps = Math.max(36, radius * 2);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (int step = 0; step < steps; step++) {
            double angle1 = Math.PI * 2.0D * step / steps;
            double angle2 = Math.PI * 2.0D * (step + 1) / steps;

            buffer.addVertex(matrix, cx, cy, 0.0F).setColor(color);
            buffer.addVertex(matrix, (float) (cx + Math.cos(angle1) * radius), (float) (cy + Math.sin(angle1) * radius), 0.0F).setColor(color);
            buffer.addVertex(matrix, (float) (cx + Math.cos(angle2) * radius), (float) (cy + Math.sin(angle2) * radius), 0.0F).setColor(color);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private int angleToIndex(double angle) {
        // Offset so that "up" = index 0
        double adjusted = angle + Math.PI / 2;
        if (adjusted < 0) adjusted += Math.PI * 2;
        double segAngle = Math.PI * 2 / SEGMENT_COUNT;
        return (int)(adjusted / segAngle) % SEGMENT_COUNT;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredIndex >= 0) { // left click to confirm
            sendCommand(COMMANDS[hoveredIndex]);
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Close on Escape or the radial key itself
        onClose();
        return true;
    }

    private void sendCommand(HorseCommand command) {
        ClientPlayNetworking.send(new RadialCommandPayload(this.horseId, command.ordinal()));
    }

    private String commandKey(HorseCommand command) {
        return switch (command) {
            case FOLLOW -> "command.icys-better-horses.follow";
            case STAY -> "command.icys-better-horses.stay";
            case RETURN_HOME -> "command.icys-better-horses.return_home";
            case SET_HOME -> "command.icys-better-horses.set_home";
        };
    }
}
