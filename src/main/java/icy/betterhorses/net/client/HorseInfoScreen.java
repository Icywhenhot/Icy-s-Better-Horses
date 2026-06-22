package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;

public class HorseInfoScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 196;
    private static final int PADDING = 12;
    private static final int ROW_HEIGHT = 16;
    private static final int LABEL_WIDTH = 70;
    private static final int BAR_WIDTH = 110;
    private static final int BAR_HEIGHT = 6;
    private static final int BAR_VALUE_GAP = 6;

    private static final int BACKDROP_COLOR = 0xE0101723;
    private static final int BACKDROP_BORDER_COLOR = 0xFF2E3A52;
    private static final int LABEL_COLOR = 0xFFB9C4D6;
    private static final int VALUE_COLOR = 0xFFFFFFFF;
    private static final int BAR_BG_COLOR = 0xFF1A2235;
    private static final int BAR_FILL_COLOR = 0xFF6CB8FF;

    // Display-unit conversions: blocks/sec for speed, blocks for jump height, HP for health. Matches the in-world stats HUD: speed_blocks_per_sec = raw * 43.2, jump_blocks = max(0, raw*6 - 1).
    private static final double SPEED_DISPLAY_FACTOR = 43.2D;

    // Mod-attainable max = vanilla base ceiling * full-bond multiplier. Bond gives up to 5 levels * 15% ADD_MULTIPLIED_BASE = +75% on top of base speed and jump.
    private static final double BOND_MAX_MULTIPLIER = 1.0D + 5 * 0.15D;
    // Bar ceilings (in display units). Bars are zero-baselined: fill = value / max.
    private static final double SPEED_MAX = 0.3375D * BOND_MAX_MULTIPLIER * SPEED_DISPLAY_FACTOR;
    private static final double JUMP_MAX = Math.max(0.0D, 1.0D * BOND_MAX_MULTIPLIER * 6.0D - 1.0D);
    private static final double HEALTH_MAX = 30.0D;

    private final AbstractHorse horse;

    public HorseInfoScreen(AbstractHorse horse) {
        super(Component.translatable("screen.icys_better_horses.info"));
        this.horse = horse;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.render(gfx, mouseX, mouseY, delta);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        gfx.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, BACKDROP_BORDER_COLOR);
        gfx.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BACKDROP_COLOR);

        Font font = this.font;
        Component title = horse.hasCustomName() ? horse.getCustomName() : getTitle();
        gfx.drawCenteredString(font, title, left + PANEL_WIDTH / 2, top + PADDING, VALUE_COLOR);

        IHorseData data = (IHorseData) horse;
        int y = top + PADDING + 18;

        drawLabel(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.gender"),
                data.bh_getGender().displayName());
        y += ROW_HEIGHT;

        drawLabel(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.breed"),
                data.bh_getBreed().displayName(data.bh_isMixedBreed()));
        y += ROW_HEIGHT;

        drawLabel(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.coat"),
                coatLabel(horse));
        y += ROW_HEIGHT + 4;

        int bond = data.bh_getBond();
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.bond"),
                bond + " / 100",
                bond / 100.0D);
        y += ROW_HEIGHT;

        double speedBlocksPerSec = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_DISPLAY_FACTOR;
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.speed"),
                String.format(java.util.Locale.ROOT, "%.1f blk/s", speedBlocksPerSec),
                speedBlocksPerSec / SPEED_MAX);
        y += ROW_HEIGHT;

        double jumpBlocks = Math.max(0.0D, horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 6.0D - 1.0D);
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.jump"),
                String.format(java.util.Locale.ROOT, "%.2f blk", jumpBlocks),
                jumpBlocks / JUMP_MAX);
        y += ROW_HEIGHT;

        double health = horse.getMaxHealth();
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys_better_horses.info.health"),
                String.format(java.util.Locale.ROOT, "%.1f HP", health),
                health / HEALTH_MAX);
    }

    private static Component coatLabel(AbstractHorse horse) {
        if (!(horse instanceof Horse h)) {
            return Component.translatable("coat.icys_better_horses.none");
        }
        Variant color = h.getVariant();
        Markings markings = h.getMarkings();
        Component colorComponent = Component.translatable("coat.icys_better_horses.color." + color.getSerializedName());
        if (markings == Markings.NONE) {
            return colorComponent;
        }
        Component markingsComponent = Component.translatable(
                "coat.icys_better_horses.markings." + markings.name().toLowerCase(java.util.Locale.ROOT));
        return Component.translatable("coat.icys_better_horses.combined", colorComponent, markingsComponent);
    }

    private void drawLabel(GuiGraphics gfx, Font font, int x, int y, Component label, Component value) {
        gfx.drawString(font, label, x, y, LABEL_COLOR, false);
        gfx.drawString(font, value, x + LABEL_WIDTH, y, VALUE_COLOR, false);
    }

    private void drawStatRow(GuiGraphics gfx, Font font, int x, int y, Component label, String value, double normalized) {
        gfx.drawString(font, label, x, y, LABEL_COLOR, false);
        int barX = x + LABEL_WIDTH;
        int barY = y + 3;
        gfx.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BG_COLOR);
        int fillWidth = (int) Math.round(Math.max(0.0D, Math.min(1.0D, normalized)) * BAR_WIDTH);
        if (fillWidth > 0) {
            gfx.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, BAR_FILL_COLOR);
        }
        gfx.drawString(font, Component.literal(value), barX + BAR_WIDTH + BAR_VALUE_GAP, y, VALUE_COLOR, false);
    }

}
