package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RadialMenuScreen extends Screen {

    private static final int SEGMENT_COUNT = 4;
    private static final int RING_INNER = 40;
    private static final int RING_OUTER = 110;
    private static final int LABEL_RADIUS = 80;

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
        gfx.fill(0, 0, width, height, 0x55000000);

        // Draw segments
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            int color = (i == hoveredIndex) ? 0xCC4a90d9 : 0xAA1a1a2e;
            drawSegment(gfx, cx, cy, i, color);
        }

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

    private void drawSegment(GuiGraphics gfx, int cx, int cy, int index, int color) {
        double segAngle = Math.PI * 2 / SEGMENT_COUNT;
        double startAngle = segAngle * index - Math.PI / 2 - segAngle / 2;
        double endAngle = startAngle + segAngle;

        int steps = 24;
        for (int s = 0; s < steps; s++) {
            double a1 = startAngle + segAngle * s / steps;
            double a2 = startAngle + segAngle * (s + 1) / steps;
            for (int r = RING_INNER; r < RING_OUTER; r += 2) {
                int x1 = cx + (int)(Math.cos(a1) * r);
                int y1 = cy + (int)(Math.sin(a1) * r);
                int x2 = cx + (int)(Math.cos(a2) * (r + 2));
                int y2 = cy + (int)(Math.sin(a2) * (r + 2));
                // Draw as a filled quad approximation using small rectangles
                int minX = Math.min(x1, x2);
                int minY = Math.min(y1, y2);
                gfx.fill(minX, minY, minX + 3, minY + 3, color);
            }
        }
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
