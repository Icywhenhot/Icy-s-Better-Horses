package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RadialMenuScreen extends Screen {

    private static final int SEGMENT_COUNT = 4;
    private static final int RING_INNER = 44;
    private static final int RING_OUTER = 110;
    private static final int RING_BACKDROP_INNER = 38;
    private static final int RING_BACKDROP_OUTER = 116;
    private static final int CENTER_RADIUS = 32;
    private static final int LABEL_RADIUS = 78;
    private static final double SEGMENT_GAP_RADIANS = Math.toRadians(2.5D);

    private static final int BASE_BACKGROUND_COLOR = 0x88060912;
    private static final int RING_BACKDROP_COLOR = 0xD0111723;
    private static final int RING_BACKDROP_SHADOW_COLOR = 0x80000000;
    private static final int INNER_DISC_COLOR = 0xE082A7E8;
    private static final int INNER_DISC_SHADOW_COLOR = 0x90000000;
    private static final int[] SEGMENT_COLORS = {
            0xC07C848E,
            0xC08A929C,
            0xC0767D87,
            0xC0939AA5
    };
    private static final int HOVERED_SEGMENT_COLOR = 0xF4D5E7FF;
    private static final int CENTER_DOT_COLOR = 0xFFE6F1FF;
    private static final int CENTER_DOT_HOVER_COLOR = 0xFFFFFFFF;
    private static final int CENTER_DOT_SHADOW_COLOR = 0xCC0C111A;

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
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        int cx = width / 2;
        int cy = height / 2;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        hoveredIndex = (dist >= RING_INNER && dist <= RING_OUTER) ? bh_angleToIndex(angle) : -1;

        // Dim background
        gfx.fill(0, 0, width, height, BASE_BACKGROUND_COLOR);

        // Drop shadow ring (offset down/right)
        bh_drawAnnulus(gfx, cx + 2, cy + 3, RING_BACKDROP_INNER, RING_BACKDROP_OUTER,
                0.0D, Math.PI * 2.0D, RING_BACKDROP_SHADOW_COLOR);
        // Backdrop ring
        bh_drawAnnulus(gfx, cx, cy, RING_BACKDROP_INNER, RING_BACKDROP_OUTER,
                0.0D, Math.PI * 2.0D, RING_BACKDROP_COLOR);

        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            double startAngle = segAngle * i - Math.PI / 2.0D - segAngle / 2.0D + SEGMENT_GAP_RADIANS;
            double endAngle = startAngle + segAngle - SEGMENT_GAP_RADIANS * 2.0D;
            int color = (i == hoveredIndex) ? HOVERED_SEGMENT_COLOR : SEGMENT_COLORS[i];
            bh_drawAnnulus(gfx, cx, cy, RING_INNER, RING_OUTER, startAngle, endAngle, color);
        }

        // Center disc shadow + disc
        bh_drawDisc(gfx, cx, cy + 2, CENTER_RADIUS + 2, INNER_DISC_SHADOW_COLOR);
        bh_drawDisc(gfx, cx, cy, CENTER_RADIUS, INNER_DISC_COLOR);

        // Labels
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            int lx = cx + LABEL_DX[i] * LABEL_RADIUS;
            int ly = cy + LABEL_DY[i] * LABEL_RADIUS;
            String text = Component.translatable(commandKey(COMMANDS[i])).getString();
            int textColor = (i == hoveredIndex) ? 0xFFFFFFFF : 0xFFD4DAE6;
            gfx.centeredText(font, text, lx, ly - font.lineHeight / 2, textColor);
        }

        // Center dot
        gfx.fill(cx - 5, cy - 5, cx + 5, cy + 5, CENTER_DOT_SHADOW_COLOR);
        gfx.fill(cx - 2, cy - 2, cx + 2, cy + 2, hoveredIndex >= 0 ? CENTER_DOT_HOVER_COLOR : CENTER_DOT_COLOR);
    }

    /** Filled disc using horizontal scanlines — produces a perfectly clean circle at any radius. */
    private void bh_drawDisc(GuiGraphicsExtractor gfx, int cx, int cy, int radius, int color) {
        int r2 = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            int xExtent = (int) Math.sqrt(r2 - dy * dy);
            gfx.fill(cx - xExtent, cy + dy, cx + xExtent + 1, cy + dy + 1, color);
        }
    }

    /**
     * Filled annular arc using horizontal scanlines, clipped to the angular range
     * [startAngle, endAngle]. Pass startAngle=0, endAngle=2π for a full ring.
     */
    private void bh_drawAnnulus(
            GuiGraphicsExtractor gfx,
            int cx,
            int cy,
            int innerRadius,
            int outerRadius,
            double startAngle,
            double endAngle,
            int color) {
        int outerR2 = outerRadius * outerRadius;
        int innerR2 = innerRadius * innerRadius;
        boolean fullCircle = (endAngle - startAngle) >= Math.PI * 2.0D - 1.0e-6D;

        for (int dy = -outerRadius; dy <= outerRadius; dy++) {
            int dy2 = dy * dy;
            if (dy2 > outerR2) continue;
            int outerX = (int) Math.sqrt(outerR2 - dy2);
            int yPx = cy + dy;

            if (dy2 >= innerR2) {
                // Single horizontal run from -outerX to +outerX
                if (fullCircle) {
                    gfx.fill(cx - outerX, yPx, cx + outerX + 1, yPx + 1, color);
                } else {
                    bh_emitClippedRun(gfx, cx - outerX, cx + outerX, yPx, dy, cx, startAngle, endAngle, color);
                }
            } else {
                int innerX = (int) Math.sqrt(innerR2 - dy2);
                // Two horizontal runs: left and right of the inner cutout
                if (fullCircle) {
                    gfx.fill(cx - outerX, yPx, cx - innerX, yPx + 1, color);
                    gfx.fill(cx + innerX + 1, yPx, cx + outerX + 1, yPx + 1, color);
                } else {
                    bh_emitClippedRun(gfx, cx - outerX, cx - innerX - 1, yPx, dy, cx, startAngle, endAngle, color);
                    bh_emitClippedRun(gfx, cx + innerX + 1, cx + outerX, yPx, dy, cx, startAngle, endAngle, color);
                }
            }
        }
    }

    /**
     * Walks x from xStart..xEnd inclusive, emitting horizontal rect fills covering pixels whose
     * angle from (cx, cy+dy) falls inside [startAngle, endAngle].
     */
    private void bh_emitClippedRun(
            GuiGraphicsExtractor gfx,
            int xStart,
            int xEnd,
            int yPx,
            int dy,
            int cx,
            double startAngle,
            double endAngle,
            int color) {
        if (xEnd < xStart) return;
        int runStart = -1;
        for (int x = xStart; x <= xEnd; x++) {
            double a = Math.atan2(dy, x - cx);
            boolean inside = bh_angleInRange(a, startAngle, endAngle);
            if (inside && runStart == -1) {
                runStart = x;
            } else if (!inside && runStart != -1) {
                gfx.fill(runStart, yPx, x, yPx + 1, color);
                runStart = -1;
            }
        }
        if (runStart != -1) {
            gfx.fill(runStart, yPx, xEnd + 1, yPx + 1, color);
        }
    }

    private boolean bh_angleInRange(double a, double start, double end) {
        double twoPi = Math.PI * 2.0D;
        double diff = a - start;
        diff = ((diff % twoPi) + twoPi) % twoPi;
        return diff <= (end - start);
    }

    /**
     * Maps a mouse angle to a segment index. Segments are centered on the cardinal directions
     * (top=0, right=1, bottom=2, left=3), so we shift by π/2 to put "up" at angle 0 and by an
     * additional half-segment so each segment's center lands at the integer index.
     */
    private int bh_angleToIndex(double angle) {
        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        double adjusted = angle + Math.PI / 2.0D + segAngle / 2.0D;
        double twoPi = Math.PI * 2.0D;
        adjusted = ((adjusted % twoPi) + twoPi) % twoPi;
        return (int) (adjusted / segAngle) % SEGMENT_COUNT;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && hoveredIndex >= 0) {
            sendCommand(COMMANDS[hoveredIndex]);
            onClose();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
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
