package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.BhNetworking;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RadialMenuScreen extends Screen {

    private static final HorseCommand[] COMMANDS = {
            HorseCommand.FOLLOW,
            HorseCommand.WANDER,
            HorseCommand.STAY,
            HorseCommand.RETURN_HOME,
            HorseCommand.SET_HOME,
    };

    private static final int SEGMENT_COUNT = COMMANDS.length;
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
            0xC0939AA5,
            0xC088929D
    };
    private static final int HOVERED_SEGMENT_COLOR = 0xF4D5E7FF;
    private static final int CENTER_DOT_COLOR = 0xFFE6F1FF;
    private static final int CENTER_DOT_HOVER_COLOR = 0xFFFFFFFF;
    private static final int CENTER_DOT_SHADOW_COLOR = 0xCC0C111A;

    private final int horseId;
    private int hoveredIndex = -1;
    private List<int[]>[] segmentRuns;

    public RadialMenuScreen(int horseId) {
        super(Component.translatable("screen.icys_better_horses.radial"));
        this.horseId = horseId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        bh_rebuildGeometry();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        if (minecraft == null || minecraft.level == null || minecraft.level.getEntity(horseId) == null) {
            onClose();
            return;
        }
        super.render(gfx, mouseX, mouseY, delta);
        if (segmentRuns == null) {
            bh_rebuildGeometry();
        }
        int cx = width / 2;
        int cy = height / 2;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        hoveredIndex = (dist >= RING_INNER && dist <= RING_OUTER) ? bh_angleToIndex(angle) : -1;

        gfx.fill(0, 0, width, height, BASE_BACKGROUND_COLOR);

        bh_drawFullRing(gfx, cx + 2, cy + 3, RING_BACKDROP_INNER, RING_BACKDROP_OUTER, RING_BACKDROP_SHADOW_COLOR);
        bh_drawFullRing(gfx, cx, cy, RING_BACKDROP_INNER, RING_BACKDROP_OUTER, RING_BACKDROP_COLOR);

        for (int i = 0; i < SEGMENT_COUNT; i++) {
            int color = (i == hoveredIndex) ? HOVERED_SEGMENT_COLOR : SEGMENT_COLORS[i % SEGMENT_COLORS.length];
            for (int[] run : segmentRuns[i]) {
                gfx.fill(run[0], run[1], run[2], run[1] + 1, color);
            }
        }

        bh_drawDisc(gfx, cx, cy + 2, CENTER_RADIUS + 2, INNER_DISC_SHADOW_COLOR);
        bh_drawDisc(gfx, cx, cy, CENTER_RADIUS, INNER_DISC_COLOR);

        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            double labelAngle = segAngle * i - Math.PI / 2.0D;
            int lx = cx + (int) Math.round(Math.cos(labelAngle) * LABEL_RADIUS);
            int ly = cy + (int) Math.round(Math.sin(labelAngle) * LABEL_RADIUS);
            String text = Component.translatable(commandKey(COMMANDS[i])).getString();
            int textColor = (i == hoveredIndex) ? 0xFFFFFFFF : 0xFFD4DAE6;
            gfx.drawCenteredString(font, text, lx, ly - font.lineHeight / 2, textColor);
        }

        gfx.fill(cx - 5, cy - 5, cx + 5, cy + 5, CENTER_DOT_SHADOW_COLOR);
        gfx.fill(cx - 2, cy - 2, cx + 2, cy + 2, hoveredIndex >= 0 ? CENTER_DOT_HOVER_COLOR : CENTER_DOT_COLOR);
    }

    // Precompute each wedge's horizontal fill runs once, so render() never re-runs atan2.
    @SuppressWarnings("unchecked")
    private void bh_rebuildGeometry() {
        int cx = width / 2;
        int cy = height / 2;
        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        segmentRuns = new List[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            double startAngle = segAngle * i - Math.PI / 2.0D - segAngle / 2.0D + SEGMENT_GAP_RADIANS;
            double endAngle = startAngle + segAngle - SEGMENT_GAP_RADIANS * 2.0D;
            segmentRuns[i] = bh_collectAnnulusRuns(cx, cy, RING_INNER, RING_OUTER, startAngle, endAngle);
        }
    }

    // Filled disc via horizontal scanlines.
    private void bh_drawDisc(GuiGraphics gfx, int cx, int cy, int radius, int color) {
        int r2 = radius * radius;
        for (int dy = -radius; dy <= radius; dy++) {
            int xExtent = (int) Math.sqrt(r2 - dy * dy);
            gfx.fill(cx - xExtent, cy + dy, cx + xExtent + 1, cy + dy + 1, color);
        }
    }

    // Full ring via horizontal scanlines (no angular clipping needed).
    private void bh_drawFullRing(GuiGraphics gfx, int cx, int cy, int innerRadius, int outerRadius, int color) {
        int outerR2 = outerRadius * outerRadius;
        int innerR2 = innerRadius * innerRadius;
        for (int dy = -outerRadius; dy <= outerRadius; dy++) {
            int dy2 = dy * dy;
            if (dy2 > outerR2) continue;
            int outerX = (int) Math.sqrt(outerR2 - dy2);
            int yPx = cy + dy;
            if (dy2 >= innerR2) {
                gfx.fill(cx - outerX, yPx, cx + outerX + 1, yPx + 1, color);
            } else {
                int innerX = (int) Math.sqrt(innerR2 - dy2);
                gfx.fill(cx - outerX, yPx, cx - innerX, yPx + 1, color);
                gfx.fill(cx + innerX + 1, yPx, cx + outerX + 1, yPx + 1, color);
            }
        }
    }

    // Collect the horizontal fill runs of an annular arc clipped to [startAngle, endAngle].
    private List<int[]> bh_collectAnnulusRuns(int cx, int cy, int innerRadius, int outerRadius,
                                              double startAngle, double endAngle) {
        List<int[]> runs = new ArrayList<>();
        int outerR2 = outerRadius * outerRadius;
        int innerR2 = innerRadius * innerRadius;
        for (int dy = -outerRadius; dy <= outerRadius; dy++) {
            int dy2 = dy * dy;
            if (dy2 > outerR2) continue;
            int outerX = (int) Math.sqrt(outerR2 - dy2);
            int yPx = cy + dy;
            if (dy2 >= innerR2) {
                bh_collectClippedRun(runs, cx - outerX, cx + outerX, yPx, dy, cx, startAngle, endAngle);
            } else {
                int innerX = (int) Math.sqrt(innerR2 - dy2);
                bh_collectClippedRun(runs, cx - outerX, cx - innerX - 1, yPx, dy, cx, startAngle, endAngle);
                bh_collectClippedRun(runs, cx + innerX + 1, cx + outerX, yPx, dy, cx, startAngle, endAngle);
            }
        }
        return runs;
    }

    // Walk x over the span, emitting {x0, y, x1} runs for pixels whose angle is inside the arc.
    private void bh_collectClippedRun(List<int[]> runs, int xStart, int xEnd, int yPx, int dy, int cx,
                                      double startAngle, double endAngle) {
        if (xEnd < xStart) return;
        int runStart = -1;
        for (int x = xStart; x <= xEnd; x++) {
            boolean inside = bh_angleInRange(Math.atan2(dy, x - cx), startAngle, endAngle);
            if (inside && runStart == -1) {
                runStart = x;
            } else if (!inside && runStart != -1) {
                runs.add(new int[]{runStart, yPx, x});
                runStart = -1;
            }
        }
        if (runStart != -1) {
            runs.add(new int[]{runStart, yPx, xEnd + 1});
        }
    }

    private boolean bh_angleInRange(double a, double start, double end) {
        double twoPi = Math.PI * 2.0D;
        double diff = a - start;
        diff = ((diff % twoPi) + twoPi) % twoPi;
        return diff <= (end - start);
    }

    // Maps a mouse angle to a segment index (segment 0 centered on "up").
    private int bh_angleToIndex(double angle) {
        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        double adjusted = angle + Math.PI / 2.0D + segAngle / 2.0D;
        double twoPi = Math.PI * 2.0D;
        adjusted = ((adjusted % twoPi) + twoPi) % twoPi;
        return (int) (adjusted / segAngle) % SEGMENT_COUNT;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double dx = mouseX - width / 2.0;
            double dy = mouseY - height / 2.0;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist >= RING_INNER && dist <= RING_OUTER) {
                sendCommand(COMMANDS[bh_angleToIndex(Math.atan2(dy, dx))]);
            }
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendCommand(HorseCommand command) {
        BhNetworking.sendToServer(new RadialCommandPayload(this.horseId, command.ordinal()));
    }

    private String commandKey(HorseCommand command) {
        return switch (command) {
            case FOLLOW -> "command.icys_better_horses.follow";
            case WANDER -> "command.icys_better_horses.wander";
            case STAY -> "command.icys_better_horses.stay";
            case RETURN_HOME -> "command.icys_better_horses.return_home";
            case SET_HOME -> "command.icys_better_horses.set_home";
        };
    }
}
