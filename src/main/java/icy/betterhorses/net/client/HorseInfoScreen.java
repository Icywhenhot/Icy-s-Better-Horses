package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class HorseInfoScreen extends Screen {

    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 180;
    private static final int PADDING = 12;
    private static final int ROW_HEIGHT = 16;
    private static final int BAR_WIDTH = 110;
    private static final int BAR_HEIGHT = 6;

    private static final int BACKDROP_COLOR = 0xE0101723;
    private static final int BACKDROP_BORDER_COLOR = 0xFF2E3A52;
    private static final int LABEL_COLOR = 0xFFB9C4D6;
    private static final int VALUE_COLOR = 0xFFFFFFFF;
    private static final int BAR_BG_COLOR = 0xFF1A2235;
    private static final int BAR_FILL_COLOR = 0xFF6CB8FF;

    // Vanilla horse attribute ranges, used for bar normalization.
    private static final double SPEED_MIN = 0.1125D;
    private static final double SPEED_MAX = 0.3375D;
    private static final double JUMP_MIN = 0.4D;
    private static final double JUMP_MAX = 1.0D;
    private static final double HEALTH_MIN = 15.0D;
    private static final double HEALTH_MAX = 30.0D;

    private final AbstractHorse horse;

    public HorseInfoScreen(AbstractHorse horse) {
        super(Component.translatable("screen.icys-better-horses.info"));
        this.horse = horse;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        graphics.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, BACKDROP_BORDER_COLOR);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BACKDROP_COLOR);

        Font font = this.font;
        Component title = horse.hasCustomName() ? horse.getCustomName() : getTitle();
        graphics.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + PADDING, VALUE_COLOR);

        IHorseData data = (IHorseData) horse;
        int y = top + PADDING + 18;

        drawLabel(graphics, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.gender"),
                data.bh_getGender().displayName());
        y += ROW_HEIGHT;

        drawLabel(graphics, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.breed"),
                data.bh_getBreed().displayName(data.bh_isMixedBreed()));
        y += ROW_HEIGHT + 4;

        int bond = data.bh_getBond();
        drawStatRow(graphics, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.bond"),
                bond + " / 100",
                bond / 100.0D);
        y += ROW_HEIGHT;

        double speed = baseValue(horse, Attributes.MOVEMENT_SPEED);
        drawStatRow(graphics, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.speed"),
                String.format("%.3f", speed),
                normalize(speed, SPEED_MIN, SPEED_MAX));
        y += ROW_HEIGHT;

        double jump = baseValue(horse, Attributes.JUMP_STRENGTH);
        drawStatRow(graphics, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.jump"),
                String.format("%.3f", jump),
                normalize(jump, JUMP_MIN, JUMP_MAX));
        y += ROW_HEIGHT;

        double health = horse.getMaxHealth();
        drawStatRow(graphics, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.health"),
                String.format("%.1f", health),
                normalize(health, HEALTH_MIN, HEALTH_MAX));
    }

    private void drawLabel(GuiGraphics graphics, Font font, int x, int y, Component label, Component value) {
        graphics.drawString(font, label, x, y, LABEL_COLOR, false);
        int valueX = x + 70;
        graphics.drawString(font, value, valueX, y, VALUE_COLOR, false);
    }

    private void drawStatRow(GuiGraphics graphics, Font font, int x, int y, Component label, String value, double normalized) {
        graphics.drawString(font, label, x, y, LABEL_COLOR, false);
        int barX = x + 70;
        int barY = y + 3;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BG_COLOR);
        int fillWidth = (int) Math.round(Math.max(0.0D, Math.min(1.0D, normalized)) * BAR_WIDTH);
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, BAR_FILL_COLOR);
        }
        graphics.drawString(font, value, barX + BAR_WIDTH + 6, y, VALUE_COLOR, false);
    }

    private static double baseValue(AbstractHorse horse, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr) {
        var inst = horse.getAttribute(attr);
        return inst == null ? 0.0D : inst.getBaseValue();
    }

    private static double normalize(double value, double min, double max) {
        if (max <= min) return 0.0D;
        return (value - min) / (max - min);
    }

}
