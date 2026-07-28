package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseManageAction;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.network.HorseManagePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;

public class HorseInfoScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 224;
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

    // Display-unit conversions: blocks/sec for speed, blocks for jump height, HP for health.
    // Matches the in-world stats HUD: speed_blocks_per_sec = raw * 43.2, jump_blocks = max(0, raw*6 - 1).
    private static final double SPEED_DISPLAY_FACTOR = 43.2D;

    // Mod-attainable max = vanilla base ceiling * full-bond multiplier.
    // Bond gives up to 5 levels * 15% ADD_MULTIPLIED_BASE = +75% on top of base speed and jump.
    private static final double BOND_MAX_MULTIPLIER = 1.0D + 5 * 0.15D;
    // Bar ceilings (in display units). Bars are zero-baselined: fill = value / max.
    private static final double SPEED_MAX = 0.3375D * BOND_MAX_MULTIPLIER * SPEED_DISPLAY_FACTOR;
    private static final double JUMP_MAX = Math.max(0.0D, 1.0D * BOND_MAX_MULTIPLIER * 6.0D - 1.0D);
    private static final double HEALTH_MAX = 30.0D;

    private static final int DISOWN_BTN_WIDTH = 110;
    private static final int DISOWN_BTN_HEIGHT = 16;
    private static final int DISOWN_BTN_BOTTOM_GAP = 12;

    private static final int CONFIRM_WIDTH = 220;
    private static final int CONFIRM_HEIGHT = 76;
    private static final int CONFIRM_BTN_WIDTH = 84;
    private static final int CONFIRM_BTN_HEIGHT = 18;

    private final AbstractHorse horse;
    private boolean confirmingDisown;

    public HorseInfoScreen(AbstractHorse horse) {
        super(Component.translatable("screen.icys-better-horses.info"));
        this.horse = horse;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        ClientHorseRoster.clearFlash();
    }

    @Override
    public void removed() {
        super.removed();
        ClientHorseRoster.clearFlash();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        // The horse this screen describes is no longer ours — nothing left to show.
        if (ClientHorseRoster.consumeSuccess(horse.getUUID(), HorseManageAction.DISOWN)) {
            onClose();
            return;
        }

        super.extractRenderState(gfx, mouseX, mouseY, delta);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        gfx.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, BACKDROP_BORDER_COLOR);
        gfx.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, BACKDROP_COLOR);

        Font font = this.font;
        Component title = horse.hasCustomName() ? horse.getCustomName() : getTitle();
        gfx.centeredText(font, title, left + PANEL_WIDTH / 2, top + PADDING, VALUE_COLOR);

        IHorseData data = (IHorseData) horse;
        int y = top + PADDING + 18;

        drawLabel(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.gender"),
                data.bh_getGender().displayName());
        y += ROW_HEIGHT;

        drawLabel(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.breed"),
                data.bh_getBreed().displayName(data.bh_isMixedBreed()));
        y += ROW_HEIGHT;

        drawLabel(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.coat"),
                coatLabel(horse));
        y += ROW_HEIGHT + 4;

        int bond = data.bh_getBond();
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.bond"),
                bond + " / 100",
                bond / 100.0D);
        y += ROW_HEIGHT;

        double speedBlocksPerSec = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_DISPLAY_FACTOR;
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.speed"),
                String.format(java.util.Locale.ROOT, "%.1f blk/s", speedBlocksPerSec),
                speedBlocksPerSec / SPEED_MAX);
        y += ROW_HEIGHT;

        double jumpBlocks = Math.max(0.0D, horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 6.0D - 1.0D);
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.jump"),
                String.format(java.util.Locale.ROOT, "%.2f blk", jumpBlocks),
                jumpBlocks / JUMP_MAX);
        y += ROW_HEIGHT;

        double health = horse.getMaxHealth();
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.health"),
                String.format(java.util.Locale.ROOT, "%.1f HP", health),
                health / HEALTH_MAX);

        renderDisownSection(gfx, font, left, top, mouseX, mouseY);
        if (confirmingDisown) {
            renderConfirm(gfx, font, mouseX, mouseY);
        }
    }

    // --- Disown ---

    private void renderDisownSection(GuiGraphicsExtractor gfx, Font font, int left, int top, int mouseX, int mouseY) {
        String flashKey = ClientHorseRoster.flashMessageKey();
        if (!flashKey.isEmpty()) {
            gfx.centeredText(font, Component.translatable(flashKey),
                    left + PANEL_WIDTH / 2, disownButtonY(top) - 13, BhScreenDraw.TEXT_ERROR);
        }

        int x = disownButtonX(left);
        int y = disownButtonY(top);
        boolean flashing = ClientHorseRoster.isFlashing();
        boolean hovered = !confirmingDisown && BhScreenDraw.inBox(mouseX, mouseY, x, y, DISOWN_BTN_WIDTH, DISOWN_BTN_HEIGHT);
        int color = flashing
                ? BhScreenDraw.BTN_ERROR
                : (hovered ? BhScreenDraw.BTN_DISOWN_HOVER : BhScreenDraw.BTN_DISOWN);
        BhScreenDraw.button(gfx, font, x, y, DISOWN_BTN_WIDTH, DISOWN_BTN_HEIGHT,
                Component.translatable("screen.icys-better-horses.manage.disown"), color, BhScreenDraw.TEXT);
    }

    private void renderConfirm(GuiGraphicsExtractor gfx, Font font, int mouseX, int mouseY) {
        gfx.fill(0, 0, this.width, this.height, 0x99000000);

        int cx = (this.width - CONFIRM_WIDTH) / 2;
        int cy = (this.height - CONFIRM_HEIGHT) / 2;
        BhScreenDraw.panel(gfx, cx, cy, CONFIRM_WIDTH, CONFIRM_HEIGHT);

        Component name = horse.hasCustomName() ? horse.getCustomName() : ((IHorseData) horse).bh_getBreed()
                .displayName(((IHorseData) horse).bh_isMixedBreed());
        gfx.centeredText(font, Component.translatable("screen.icys-better-horses.manage.confirm_title"),
                cx + CONFIRM_WIDTH / 2, cy + 12, BhScreenDraw.TEXT);
        gfx.centeredText(font, Component.translatable("screen.icys-better-horses.manage.confirm_body", name),
                cx + CONFIRM_WIDTH / 2, cy + 26, BhScreenDraw.TEXT_MUTED);

        int btnY = confirmButtonY();
        boolean yesHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);
        boolean cancelHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);

        BhScreenDraw.button(gfx, font, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT,
                Component.translatable("screen.icys-better-horses.manage.confirm_yes"),
                yesHovered ? BhScreenDraw.BTN_DISOWN_HOVER : BhScreenDraw.BTN_DISOWN, BhScreenDraw.TEXT);
        BhScreenDraw.button(gfx, font, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT,
                Component.translatable("screen.icys-better-horses.manage.confirm_cancel"),
                cancelHovered ? BhScreenDraw.BTN_NEUTRAL_HOVER : BhScreenDraw.BTN_NEUTRAL, BhScreenDraw.TEXT);
    }

    private int disownButtonX(int left) {
        return left + (PANEL_WIDTH - DISOWN_BTN_WIDTH) / 2;
    }

    private int disownButtonY(int top) {
        return top + PANEL_HEIGHT - DISOWN_BTN_HEIGHT - DISOWN_BTN_BOTTOM_GAP;
    }

    private int confirmButtonY() {
        return (this.height - CONFIRM_HEIGHT) / 2 + CONFIRM_HEIGHT - CONFIRM_BTN_HEIGHT - 10;
    }

    private int confirmYesX() {
        return (this.width - CONFIRM_WIDTH) / 2 + 12;
    }

    private int confirmCancelX() {
        return (this.width - CONFIRM_WIDTH) / 2 + CONFIRM_WIDTH - CONFIRM_BTN_WIDTH - 12;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        if (confirmingDisown) {
            int btnY = confirmButtonY();
            if (BhScreenDraw.inBox(event.x(), event.y(), confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT)) {
                confirmingDisown = false;
                ClientHorseRoster.clearFlash();
                ClientPlayNetworking.send(
                        new HorseManagePayload(horse.getUUID(), HorseManageAction.DISOWN.ordinal()));
                return true;
            }
            if (BhScreenDraw.inBox(event.x(), event.y(), confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT)) {
                confirmingDisown = false;
                return true;
            }
            // Modal: swallow everything else.
            return true;
        }

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        if (BhScreenDraw.inBox(event.x(), event.y(),
                disownButtonX(left), disownButtonY(top), DISOWN_BTN_WIDTH, DISOWN_BTN_HEIGHT)) {
            ClientHorseRoster.clearFlash();
            confirmingDisown = true;
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (confirmingDisown) {
            if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                confirmingDisown = false;
            }
            return true;
        }
        return super.keyPressed(event);
    }

    private static Component coatLabel(AbstractHorse horse) {
        if (!(horse instanceof Horse h)) {
            return Component.translatable("coat.icys-better-horses.none");
        }
        Variant color = h.getVariant();
        Markings markings = h.getMarkings();
        Component colorComponent = Component.translatable("coat.icys-better-horses.color." + color.getSerializedName());
        if (markings == Markings.NONE) {
            return colorComponent;
        }
        Component markingsComponent = Component.translatable(
                "coat.icys-better-horses.markings." + markings.name().toLowerCase(java.util.Locale.ROOT));
        return Component.translatable("coat.icys-better-horses.combined", colorComponent, markingsComponent);
    }

    private void drawLabel(GuiGraphicsExtractor gfx, Font font, int x, int y, Component label, Component value) {
        gfx.text(font, label, x, y, LABEL_COLOR, false);
        gfx.text(font, value, x + LABEL_WIDTH, y, VALUE_COLOR, false);
    }

    private void drawStatRow(GuiGraphicsExtractor gfx, Font font, int x, int y, Component label, String value, double normalized) {
        gfx.text(font, label, x, y, LABEL_COLOR, false);
        int barX = x + LABEL_WIDTH;
        int barY = y + 3;
        gfx.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, BAR_BG_COLOR);
        int fillWidth = (int) Math.round(Math.max(0.0D, Math.min(1.0D, normalized)) * BAR_WIDTH);
        if (fillWidth > 0) {
            gfx.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, BAR_FILL_COLOR);
        }
        gfx.text(font, Component.literal(value), barX + BAR_WIDTH + BAR_VALUE_GAP, y, VALUE_COLOR, false);
    }

}
