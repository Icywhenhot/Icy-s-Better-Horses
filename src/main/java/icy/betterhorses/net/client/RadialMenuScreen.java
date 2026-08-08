package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

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

    // tinted to the rest of the mod's UI rather than the old cool blue-grey
    private static final int BASE_BACKGROUND_COLOR = 0x55140D07;
    // light brown, for the ring the wedges sit
    private static final int RING_BACKDROP_COLOR = 0x4C5A3A1C;
    // thin warm arcs along the ring's inner and outer edges, the detail that reads as a lit rim
    private static final int RING_RIM_COLOR = 0x66A37236;
    // parchment, for the five command panels
    private static final int SEGMENT_COLOR = 0x4CE0C1A6;
    private static final int SEGMENT_HOVER_COLOR = 0x99F2DFC4;
    // each segment's own outer rim, warming and brightening as it's hovered
    private static final int SEGMENT_RIM_COLOR = 0x40CCA989;
    private static final int SEGMENT_RIM_HOVER_COLOR = 0xB2FFF0D8;
    // light red, for the hub
    private static final int CENTER_DISC_COLOR = 0x66A83B18;
    private static final int CENTER_RIM_COLOR = 0x80BF360C;
    private static final int CENTER_DOT_COLOR = 0x99F2DFC4;
    private static final int CENTER_DOT_HOVER_COLOR = 0xFFFFF3E0;

    private static final int LABEL_COLOR = 0xFFEBD9BE;
    private static final int LABEL_HOVER_COLOR = 0xFFFFFFFF;

    // thickness of the rim arcs, and how far a hovered wedge swells outward
    private static final float RIM_THICKNESS = 1.5F;
    private static final float HOVER_PUSH = 4F;
    private static final float HOVER_TAU = 0.05F;   // hover fade time-constant; smaller = snappier

    private final int horseId;
    private int hoveredIndex = -1;
    private final BhAnim.Lift hover = new BhAnim.Lift();
    private long bhOpenMs;

    public RadialMenuScreen(int horseId) {
        super(Component.translatable("screen.icys-better-horses.radial"));
        this.horseId = horseId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        bhOpenMs = System.currentTimeMillis();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        if (minecraft == null || minecraft.level == null || minecraft.level.getEntity(horseId) == null) {
            onClose();
            return;
        }
        int cx = width / 2;
        int cy = height / 2;

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        hoveredIndex = (dist >= RING_INNER && dist <= RING_OUTER)
                ? bh_angleToIndex(Math.atan2(dy, dx)) : -1;

        // entrance: the wheel blooms outward from the centre over a dimmed, blurred world
        float t = BhAnim.clamp01((System.currentTimeMillis() - bhOpenMs) / 200f);
        gfx.fill(0, 0, width, height, BhAnim.fade(BASE_BACKGROUND_COLOR, t));
        hover.beginFrame(HOVER_TAU);

        var pose = gfx.pose();
        pose.pushMatrix();
        BhAnim.enter(pose, BhAnim.easeOutBack(t), cx, cy, 0f, 0.85f);

        BhVector.Builder mesh = new BhVector.Builder();
        BhVector.ring(mesh, cx, cy, RING_BACKDROP_INNER, RING_BACKDROP_OUTER, RING_BACKDROP_COLOR, BhVector.FEATHER);
        BhVector.ring(mesh, cx, cy, RING_BACKDROP_OUTER - RIM_THICKNESS, RING_BACKDROP_OUTER,
                RING_RIM_COLOR, BhVector.FEATHER);
        BhVector.ring(mesh, cx, cy, RING_BACKDROP_INNER, RING_BACKDROP_INNER + RIM_THICKNESS,
                RING_RIM_COLOR, BhVector.FEATHER);

        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        float[] hoverAmount = new float[SEGMENT_COUNT];
        for (int i = 0; i < SEGMENT_COUNT; i++) {
            // hover fades in and out through the shared spring, so the highlight never snaps
            hoverAmount[i] = hover.get(i, i == hoveredIndex, 1f);
            double start = segAngle * i - Math.PI / 2.0D - segAngle / 2.0D + SEGMENT_GAP_RADIANS;
            double end = start + segAngle - SEGMENT_GAP_RADIANS * 2.0D;

            // a hovered wedge swells outward a few pixels as well as brightening
            float outer = RING_OUTER + HOVER_PUSH * hoverAmount[i];
            int fill = bh_mixColor(SEGMENT_COLOR, SEGMENT_HOVER_COLOR, hoverAmount[i]);
            int rim = bh_mixColor(SEGMENT_RIM_COLOR, SEGMENT_RIM_HOVER_COLOR, hoverAmount[i]);
            BhVector.wedge(mesh, cx, cy, RING_INNER, outer, start, end, fill, BhVector.FEATHER);
            BhVector.wedge(mesh, cx, cy, outer - RIM_THICKNESS, outer, start, end, rim, BhVector.FEATHER);
        }

        BhVector.disc(mesh, cx, cy, CENTER_RADIUS, CENTER_DISC_COLOR, BhVector.FEATHER);
        BhVector.ring(mesh, cx, cy, CENTER_RADIUS - RIM_THICKNESS, CENTER_RADIUS,
                CENTER_RIM_COLOR, BhVector.FEATHER);
        BhVector.disc(mesh, cx, cy, 3.5F,
                hoveredIndex >= 0 ? CENTER_DOT_HOVER_COLOR : CENTER_DOT_COLOR, BhVector.FEATHER);
        BhVector.submit(gfx, mesh);

        for (int i = 0; i < SEGMENT_COUNT; i++) {
            double labelAngle = segAngle * i - Math.PI / 2.0D;
            float labelRadius = LABEL_RADIUS + HOVER_PUSH * 0.5F * hoverAmount[i];
            int lx = cx + Math.round((float) Math.cos(labelAngle) * labelRadius);
            int ly = cy + Math.round((float) Math.sin(labelAngle) * labelRadius);
            String text = Component.translatable(commandKey(COMMANDS[i])).getString();
            int textColor = bh_mixColor(LABEL_COLOR, LABEL_HOVER_COLOR, hoverAmount[i]);
            gfx.centeredText(font, text, lx, ly - font.lineHeight / 2, textColor);
        }

        pose.popMatrix();
    }

    // channel-wise blend of two ARGB colours, k 0 = first, 1 = second
    private static int bh_mixColor(int from, int to, float k) {
        float m = BhAnim.clamp01(k);
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            out |= Math.round(a + (b - a) * m) << shift;
        }
        return out;
    }

    // maps a mouse angle to a segment index (segment 0 centered on "up")
    private int bh_angleToIndex(double angle) {
        double segAngle = Math.PI * 2.0D / SEGMENT_COUNT;
        double adjusted = angle + Math.PI / 2.0D + segAngle / 2.0D;
        double twoPi = Math.PI * 2.0D;
        adjusted = ((adjusted % twoPi) + twoPi) % twoPi;
        return (int) (adjusted / segAngle) % SEGMENT_COUNT;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            double dx = event.x() - width / 2.0;
            double dy = event.y() - height / 2.0;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist >= RING_INNER && dist <= RING_OUTER) {
                sendCommand(COMMANDS[bh_angleToIndex(Math.atan2(dy, dx))]);
            }
            onClose();
            return true;
        }
        return super.mouseReleased(event);
    }

    private void sendCommand(HorseCommand command) {
        ClientPlayNetworking.send(new RadialCommandPayload(this.horseId, command.ordinal()));
    }

    private String commandKey(HorseCommand command) {
        return switch (command) {
            case FOLLOW -> "command.icys-better-horses.follow";
            case WANDER -> "command.icys-better-horses.wander";
            case STAY -> "command.icys-better-horses.stay";
            case RETURN_HOME -> "command.icys-better-horses.return_home";
            case SET_HOME -> "command.icys-better-horses.set_home";
        };
    }
}
