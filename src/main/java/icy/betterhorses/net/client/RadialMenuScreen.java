package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.AbilityType;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.network.RadialCommandPayload;
import icy.betterhorses.net.BhNetworking;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.CommandType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RadialMenuScreen extends Screen {

    @SuppressWarnings("unchecked")
    private static final ResourceKey<CommandType>[] COMMANDS = new ResourceKey[] {
            BhContent.COMMAND_FOLLOW.getKey(),
            BhContent.COMMAND_WANDER.getKey(),
            BhContent.COMMAND_STAY.getKey(),
            BhContent.COMMAND_RETURN_HOME.getKey(),
            BhContent.COMMAND_SET_HOME.getKey(),
    };
    private static final int RING_INNER = 44;
    private static final int RING_OUTER = 110;
    private static final int RING_BACKDROP_INNER = 38;
    private static final int RING_BACKDROP_OUTER = 116;
    private static final int CENTER_RADIUS = 32;
    private static final int LABEL_RADIUS = 78;
    private static final double SEGMENT_GAP_RADIANS = Math.toRadians(2.5D);

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

    private static final float RIM_THICKNESS = 1.5F;
    private static final float HOVER_PUSH = 4F;
    private static final float HOVER_TAU = 0.05F;

    private final int horseId;
    private int hoveredIndex = -1;
    private final BhAnim.Lift hover = new BhAnim.Lift();
    private long bhOpenMs;

    private final List<Entry> commands;
    private final int segmentCount;

    public RadialMenuScreen(int horseId) {
        super(Component.translatable("screen.icys-better-horses.radial"));
        this.horseId = horseId;
        this.commands = wheelFor(horseId);
        this.segmentCount = this.commands.size();
    }

    private record Entry(ResourceKey<CommandType> command, String ability, Component label) {}

    private static List<Entry> wheelFor(int horseId) {
        List<Entry> entries = new ArrayList<>();
        for (ResourceKey<CommandType> command : COMMANDS) {
            entries.add(new Entry(command, "", CommandType.displayName(command)));
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null
                || !(mc.level.getEntity(horseId) instanceof AbstractHorse horse)) return entries;
        IHorseData data = IHorseData.of(horse);
        if (CommandType.toggleable(data.bh_getBreedKey())) {
            entries.add(new Entry(BhContent.COMMAND_ABILITY.getKey(), "",
                    CommandType.displayName(BhContent.COMMAND_ABILITY.getKey())));
        }
        for (ResourceKey<AbilityType> ability : ((IHorseAbilityHost) horse).bh_activeAbilities()) {
            entries.add(new Entry(BhContent.COMMAND_ABILITY.getKey(), ability.location().toString(),
                    Component.translatable("ability." + ability.location().getNamespace() + "."
                            + ability.location().getPath())));
        }
        BhRegistries.commandTypeRegistry().getKeys().stream().sorted(Comparator.comparing(Object::toString))
                .forEach(id -> {
                    CommandType type = BhRegistries.commandTypeRegistry().getValue(id);
                    if (type.custom() && type.available(horse, mc.player)) {
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

        double dx = mouseX - cx;
        double dy = mouseY - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        hoveredIndex = (dist >= RING_INNER && dist <= RING_OUTER)
                ? bh_angleToIndex(Math.atan2(dy, dx)) : -1;

        float t = BhAnim.clamp01((System.currentTimeMillis() - bhOpenMs) / 200f);
        gfx.fill(0, 0, width, height, BhAnim.fade(BASE_BACKGROUND_COLOR, t));
        hover.beginFrame(HOVER_TAU);

        var pose = gfx.pose();
        pose.pushPose();
        BhAnim.enter(pose, BhAnim.easeOutBack(t), cx, cy, 0f, 0.85f);

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

        pose.popPose();
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
            double dx = mouseX - width / 2.0;
            double dy = mouseY - height / 2.0;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist >= RING_INNER && dist <= RING_OUTER) {
                sendCommand(this.commands.get(bh_angleToIndex(Math.atan2(dy, dx))));
            }
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendCommand(Entry entry) {
        BhNetworking.sendToServer(new RadialCommandPayload(this.horseId,
                entry.command().location().toString(), entry.ability()));
    }
}
