package icy.betterhorses.net.client;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.network.RadialCommandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.CommandType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RadialMenuScreen extends Screen {

    @SuppressWarnings("unchecked")
    private static final ResourceKey<CommandType>[] COMMANDS = new ResourceKey[] {
            BhContent.COMMAND_FOLLOW.key(),
            BhContent.COMMAND_WANDER.key(),
            BhContent.COMMAND_STAY.key(),
            BhContent.COMMAND_RETURN_HOME.key(),
            BhContent.COMMAND_SET_HOME.key(),
    };
    private static final int RING_INNER = 44;
    private static final int RING_OUTER = 110;
    private static final int RING_BACKDROP_INNER = 38;
    private static final int RING_BACKDROP_OUTER = 116;
    private static final int CENTER_RADIUS = 32;
    private static final int LABEL_RADIUS = 78;
    private static final double SEGMENT_GAP_RADIANS = Math.toRadians(2.5D);

    private static final ResourceLocation SHOE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "textures/gui/radial_horseshoe.png");
    private static final int SHOE_TEXTURE_SIZE = 168;
    private static final int SHOE_SIZE = 168;
    private static final float SHOE_SCALE = (float) SHOE_SIZE / SHOE_TEXTURE_SIZE;

    private static final float[][] NAIL_UV = {
            {28.5F, 60.5F},
            {138.5F, 60.5F},
            {24.5F, 101.5F},
            {142.5F, 101.5F},
            {83.5F, 148.5F},
    };

    private static final int BASE_BACKGROUND_COLOR = 0x55140D07;
    private static final int RING_BACKDROP_COLOR = 0x4C5A3A1C;
    private static final int RING_RIM_COLOR = 0x66A37236;
    private static final int SEGMENT_COLOR = 0x4CE0C1A6;
    private static final int SEGMENT_HOVER_COLOR = 0x99F2DFC4;
    private static final int SEGMENT_RIM_COLOR = 0x40CCA989;
    private static final int SEGMENT_RIM_HOVER_COLOR = 0xB2FFF0D8;
    private static final int CENTER_DISC_COLOR = 0x66A83B18;
    private static final int CENTER_RIM_COLOR = 0x80BF360C;
    private static final int CENTER_DOT_COLOR = 0x99F2DFC4;
    private static final int CENTER_DOT_HOVER_COLOR = 0xFFFFF3E0;

    private static final int LABEL_COLOR = 0xFFEBD9BE;
    private static final int LABEL_HOVER_COLOR = 0xFFFFFFFF;

    private static final int NAIL_GLOW_COLOR = 0xFFFFE9BE;
    private static final int LEADER_COLOR = 0xFFE8D4AE;
    private static final float NAIL_GLOW_RADIUS = 5.5F;
    private static final float LEADER_THICKNESS = 1.6F;
    private static final float LEADER_RISE = 22F;
    private static final float LEADER_RUN = 26F;
    private static final float LEADER_TAIL = 30F;
    private static final float LABEL_POP = 7F;
    private static final float BOTTOM_LEADER_DROP = 26F;

    private static final float RIM_THICKNESS = 1.5F;
    private static final float HOVER_PUSH = 4F;
    private static final float HOVER_TAU = 0.05F;

    private final int horseId;
    private final BhConfig.RadialStyle style;
    private int hoveredIndex = -1;
    private final BhAnim.Lift hover = new BhAnim.Lift();
    private long bhOpenMs;

    private final List<Entry> commands;
    private final int segmentCount;

    public RadialMenuScreen(int horseId) {
        super(Component.translatable("screen.icys-better-horses.radial"));
        this.horseId = horseId;
        this.style = BhConfig.radialStyle();
        this.commands = this.style == BhConfig.RadialStyle.HORSESHOE ? fixedWheel() : wheelFor(horseId);
        this.segmentCount = this.commands.size();
    }

    private record Entry(ResourceKey<CommandType> command, String ability, Component label) {}

    private static List<Entry> fixedWheel() {
        List<Entry> entries = new ArrayList<>();
        for (ResourceKey<CommandType> command : COMMANDS) {
            entries.add(new Entry(command, "", CommandType.displayName(command)));
        }
        return entries;
    }

    private static List<Entry> wheelFor(int horseId) {
        List<Entry> entries = fixedWheel();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || !(mc.level.getEntity(horseId) instanceof AbstractHorse horse)
                || !BhHorseKind.managed(horse)) {
            return entries;
        }
        BhRegistries.commandTypeRegistry().keySet().stream().sorted(Comparator.comparing(Object::toString))
                .forEach(id -> {
                    CommandType type = BhRegistries.commandTypeRegistry().get(id);
                    if (type != null && type.custom() && type.available(horse, mc.player)) {
                        ResourceKey<CommandType> key = ResourceKey.create(BhRegistries.COMMAND_TYPES, id);
                        entries.add(new Entry(key, "", CommandType.displayName(key)));
                    }
                });
        return entries;
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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        if (minecraft == null || minecraft.level == null || minecraft.level.getEntity(horseId) == null) {
            onClose();
            return;
        }
        int cx = width / 2;
        int cy = height / 2;
        hoveredIndex = pick(mouseX, mouseY, cx, cy);

        float t = BhAnim.clamp01((System.currentTimeMillis() - bhOpenMs) / 200f);
        gfx.fill(0, 0, width, height, BhAnim.fade(BASE_BACKGROUND_COLOR, t));
        hover.beginFrame(HOVER_TAU);

        var pose = gfx.pose();
        pose.pushPose();
        BhAnim.enter(pose, BhAnim.easeOutBack(t), cx, cy, 0f, 0.85f);
        if (style == BhConfig.RadialStyle.HORSESHOE) {
            drawShoe(gfx, cx, cy, t);
        } else {
            drawDial(gfx, cx, cy);
        }
        pose.popPose();
    }

    private void drawShoe(GuiGraphics gfx, int cx, int cy, float t) {
        int left = cx - SHOE_SIZE / 2;
        int top = cy - SHOE_SIZE / 2;
        BhScreenDraw.panelTexture(gfx, left, top, SHOE_SIZE, SHOE_SIZE, SHOE_TEXTURE, t);

        float[] amount = new float[this.segmentCount];
        BhVector.Builder mesh = new BhVector.Builder();
        for (int i = 0; i < this.segmentCount; i++) {
            amount[i] = hover.get(i, i == hoveredIndex, 1f);
            if (amount[i] <= 0.03F) continue;

            float nx = nailX(i, left);
            float ny = nailY(i, top);
            BhVector.disc(mesh, nx, ny, NAIL_GLOW_RADIUS,
                    BhVector.scaleAlpha(NAIL_GLOW_COLOR, amount[i]), BhVector.FEATHER);

            int leader = BhVector.scaleAlpha(LEADER_COLOR, amount[i]);
            float pop = BhAnim.easeOutBack(amount[i]);
            if (i == 4) {
                float endY = ny + BOTTOM_LEADER_DROP * pop;
                BhVector.line(mesh, nx, ny, nx, endY, LEADER_THICKNESS, leader, BhVector.FEATHER);
            } else {
                float dir = i == 0 || i == 2 ? -1F : 1F;
                float elbowX = nx + dir * LEADER_RUN * pop;
                float elbowY = ny - LEADER_RISE * pop;
                float tailX = elbowX + dir * LEADER_TAIL * pop;
                BhVector.line(mesh, nx, ny, elbowX, elbowY, LEADER_THICKNESS, leader, BhVector.FEATHER);
                BhVector.line(mesh, elbowX, elbowY, tailX, elbowY, LEADER_THICKNESS, leader, BhVector.FEATHER);
            }
        }
        BhVector.submit(gfx, mesh);

        for (int i = 0; i < this.segmentCount; i++) {
            if (amount[i] <= 0.03F) continue;
            String text = this.commands.get(i).label().getString();
            int color = (Math.round(255f * amount[i]) << 24) | (LABEL_HOVER_COLOR & 0xFFFFFF);
            float pop = BhAnim.easeOutBack(amount[i]);
            float nx = nailX(i, left);
            float ny = nailY(i, top);
            if (i == 4) {
                float ly = ny + BOTTOM_LEADER_DROP * pop + 3F;
                gfx.drawCenteredString(font, text, Math.round(nx), Math.round(ly), color);
            } else {
                float dir = i == 0 || i == 2 ? -1F : 1F;
                float tailX = nx + dir * (LEADER_RUN + LEADER_TAIL) * pop + dir * LABEL_POP * (1F - pop);
                float ly = ny - LEADER_RISE * pop - font.lineHeight / 2F;
                int tx = Math.round(dir < 0 ? tailX - 3F - font.width(text) : tailX + 3F);
                gfx.drawString(font, text, tx, Math.round(ly), color, true);
            }
        }
    }

    private void drawDial(GuiGraphics gfx, int cx, int cy) {
        BhVector.Builder mesh = new BhVector.Builder();
        BhVector.ring(mesh, cx, cy, RING_BACKDROP_INNER, RING_BACKDROP_OUTER, RING_BACKDROP_COLOR, BhVector.FEATHER);
        BhVector.ring(mesh, cx, cy, RING_BACKDROP_OUTER - RIM_THICKNESS, RING_BACKDROP_OUTER,
                RING_RIM_COLOR, BhVector.FEATHER);
        BhVector.ring(mesh, cx, cy, RING_BACKDROP_INNER, RING_BACKDROP_INNER + RIM_THICKNESS,
                RING_RIM_COLOR, BhVector.FEATHER);

        double segAngle = Math.PI * 2.0D / this.segmentCount;
        float[] hoverAmount = new float[this.segmentCount];
        for (int i = 0; i < this.segmentCount; i++) {
            hoverAmount[i] = hover.get(i, i == hoveredIndex, 1f);
            double start = segAngle * i - Math.PI / 2.0D - segAngle / 2.0D + SEGMENT_GAP_RADIANS;
            double end = start + segAngle - SEGMENT_GAP_RADIANS * 2.0D;

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

        for (int i = 0; i < this.segmentCount; i++) {
            double labelAngle = segAngle * i - Math.PI / 2.0D;
            float labelRadius = LABEL_RADIUS + HOVER_PUSH * 0.5F * hoverAmount[i];
            int lx = cx + Math.round((float) Math.cos(labelAngle) * labelRadius);
            int ly = cy + Math.round((float) Math.sin(labelAngle) * labelRadius);
            String text = this.commands.get(i).label().getString();
            int textColor = bh_mixColor(LABEL_COLOR, LABEL_HOVER_COLOR, hoverAmount[i]);
            gfx.drawCenteredString(font, text, lx, ly - font.lineHeight / 2, textColor);
        }
    }

    private static float nailX(int index, int left) {
        return left + NAIL_UV[index][0] * SHOE_SCALE;
    }

    private static float nailY(int index, int top) {
        return top + NAIL_UV[index][1] * SHOE_SCALE;
    }

    private int pick(double mouseX, double mouseY, int cx, int cy) {
        if (style == BhConfig.RadialStyle.HORSESHOE) {
            return nearestNail(mouseX, mouseY, cx, cy);
        }
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist >= RING_INNER && dist <= RING_OUTER ? bh_angleToIndex(Math.atan2(dy, dx)) : -1;
    }

    private int nearestNail(double mouseX, double mouseY, int cx, int cy) {
        int left = cx - SHOE_SIZE / 2;
        int top = cy - SHOE_SIZE / 2;
        if (mouseX < left || mouseX > left + SHOE_SIZE || mouseY < top || mouseY > top + SHOE_SIZE) {
            return -1;
        }
        int best = -1;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < NAIL_UV.length; i++) {
            double dx = mouseX - nailX(i, left);
            double dy = mouseY - nailY(i, top);
            double dist = dx * dx + dy * dy;
            if (dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

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

    private int bh_angleToIndex(double angle) {
        double segAngle = Math.PI * 2.0D / this.segmentCount;
        double adjusted = angle + Math.PI / 2.0D + segAngle / 2.0D;
        double twoPi = Math.PI * 2.0D;
        adjusted = ((adjusted % twoPi) + twoPi) % twoPi;
        return (int) (adjusted / segAngle) % this.segmentCount;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int index = pick(mouseX, mouseY, width / 2, height / 2);
            if (index >= 0 && index < this.segmentCount) {
                sendCommand(this.commands.get(index));
            }
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendCommand(Entry entry) {
        ClientPlayNetworking.send(new RadialCommandPayload(this.horseId,
                entry.command().location().toString(), entry.ability()));
    }
}
