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
    private static final int LABEL_INDENT = 22;
    private static final int TITLE_Y = 13;
    private static final int CONTENT_TOP = 50;
    private static final int BAR_WIDTH = 110;
    private static final int BAR_HEIGHT = 6;
    private static final int BAR_VALUE_GAP = 6;

    private static final int LABEL_COLOR = 0xFF6B4A2C;
    private static final int VALUE_COLOR = 0xFF3A2714;
    private static final int BAR_BG_COLOR = 0xFF3B2A17;
    private static final int BAR_FILL_COLOR = 0xFFA06A34;

    private static final double SPEED_DISPLAY_FACTOR = 43.2D;

    private static final double BOND_MAX_MULTIPLIER = 1.0D + 5 * 0.15D;
    private static final double SPEED_MAX = 0.3375D * BOND_MAX_MULTIPLIER * SPEED_DISPLAY_FACTOR;
    private static final double JUMP_MAX = Math.max(0.0D, 1.0D * BOND_MAX_MULTIPLIER * 6.0D - 1.0D);
    private static final double HEALTH_MAX = 30.0D;

    private static final int DISOWN_BTN_WIDTH = 110;
    private static final int DISOWN_BTN_HEIGHT = 24;
    private static final int DISOWN_BTN_BOTTOM_GAP = 6;
    private static final int DISOWN_TEXT_COLOR = 0xFF3A2714;
    private static final int DISOWN_FLASH_TINT = 0xFFE85C5C;

    private static final int CONFIRM_WIDTH = 220;
    private static final int CONFIRM_HEIGHT = 76;
    private static final int CONFIRM_BTN_WIDTH = 84;
    private static final int CONFIRM_BTN_HEIGHT = 20;
    private static final int CANCEL_TEXT_COLOR = 0xFFEDE6DA;

    private static final float ENTER_MS = 220f;
    private static final float ENTER_RISE = 10f;
    private static final float ENTER_SCALE = 0.95f;
    private static final float CLOSE_MS = 150f;
    private static final float LIFT_PX = 2f;
    private static final float LIFT_TAU = 0.045f;
    private static final float PRESS_DEPTH = 0.08f;
    private static final float PRESS_MS = 130f;
    private static final float STAT_DELAY_MS = 120f;
    private static final float STAT_MS = 520f;

    private final AbstractHorse horse;
    private boolean confirmingDisown;
    private long bhOpenMs;
    private long bhConfirmOpenMs;
    private boolean bhClosing;
    private long bhCloseMs;
    private final BhAnim.Lift lift = new BhAnim.Lift();
    private final BhAnim.Press press = new BhAnim.Press();

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
        bhOpenMs = System.currentTimeMillis();
    }

    @Override
    public void onClose() {
        if (bhClosing) {
            super.onClose();
            return;
        }
        bhClosing = true;
        bhCloseMs = System.currentTimeMillis();
    }

    @Override
    public void removed() {
        super.removed();
        ClientHorseRoster.clearFlash();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        if (ClientHorseRoster.consumeSuccess(horse.getUUID(), HorseManageAction.DISOWN)) {
            onClose();
            return;
        }

        super.extractRenderState(gfx, mouseX, mouseY, delta);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;

        float vis;
        if (bhClosing) {
            float cp = (System.currentTimeMillis() - bhCloseMs) / CLOSE_MS;
            if (cp >= 1f) {
                super.onClose();
                return;
            }
            vis = 1f - BhAnim.easeInCubic(cp);
        } else {
            vis = BhAnim.easeOutCubic((System.currentTimeMillis() - bhOpenMs) / ENTER_MS);
        }
        float statP = bhClosing ? 1f
                : BhAnim.easeOutCubic((System.currentTimeMillis() - bhOpenMs - STAT_DELAY_MS) / STAT_MS);
        lift.beginFrame(LIFT_TAU);

        var pose = gfx.pose();
        pose.pushMatrix();
        BhAnim.enter(pose, vis, left + PANEL_WIDTH / 2f, top + PANEL_HEIGHT / 2f, ENTER_RISE, ENTER_SCALE);

        BhScreenDraw.panelTexture(gfx, left, top, PANEL_WIDTH, PANEL_HEIGHT, BhScreenDraw.SCREEN_INFO_TEXTURE, vis);

        Font font = this.font;
        Component title = horse.hasCustomName() ? horse.getCustomName() : getTitle();
        gfx.text(font, title, left + PANEL_WIDTH / 2 - font.width(title) / 2, top + TITLE_Y, VALUE_COLOR, false);

        IHorseData data = (IHorseData) horse;
        int y = top + CONTENT_TOP;

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

        int bond = Math.round(data.bh_getBond() * statP);
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.bond"),
                bond + " / 100",
                bond / 100.0D);
        y += ROW_HEIGHT;

        double speedBlocksPerSec = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_DISPLAY_FACTOR * statP;
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.speed"),
                String.format(java.util.Locale.ROOT, "%.1f blk/s", speedBlocksPerSec),
                speedBlocksPerSec / SPEED_MAX);
        y += ROW_HEIGHT;

        double jumpBlocks = Math.max(0.0D, horse.getAttributeValue(Attributes.JUMP_STRENGTH) * 6.0D - 1.0D) * statP;
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.jump"),
                String.format(java.util.Locale.ROOT, "%.2f blk", jumpBlocks),
                jumpBlocks / JUMP_MAX);
        y += ROW_HEIGHT;

        double health = horse.getMaxHealth() * statP;
        drawStatRow(gfx, font, left + PADDING, y,
                Component.translatable("screen.icys-better-horses.info.health"),
                String.format(java.util.Locale.ROOT, "%.1f HP", health),
                health / HEALTH_MAX);

        renderDisownSection(gfx, font, left, top, mouseX, mouseY);

        pose.popMatrix();

        if (confirmingDisown) {
            renderConfirm(gfx, font, mouseX, mouseY);
        }
    }

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
        int tint = flashing ? DISOWN_FLASH_TINT : 0xFFFFFFFF;

        float ly = lift.get("disown", hovered, LIFT_PX);
        float sc = press.scale("disown", PRESS_DEPTH, PRESS_MS);

        BhScreenDraw.textureShadow(gfx, BhScreenDraw.DISOWN_BUTTON_TEXTURE, x, y,
                DISOWN_BTN_WIDTH, DISOWN_BTN_HEIGHT, ly, 1f);

        var pose = gfx.pose();
        pose.pushMatrix();
        pose.translate(0f, -ly);
        if (sc != 1f) {
            float ccx = x + DISOWN_BTN_WIDTH / 2f;
            float ccy = y + DISOWN_BTN_HEIGHT / 2f;
            pose.translate(ccx, ccy);
            pose.scale(sc, sc);
            pose.translate(-ccx, -ccy);
        }
        BhScreenDraw.textureButton(gfx, font, BhScreenDraw.DISOWN_BUTTON_TEXTURE, x, y, DISOWN_BTN_WIDTH, DISOWN_BTN_HEIGHT,
                Component.translatable("screen.icys-better-horses.manage.disown"), DISOWN_TEXT_COLOR, tint);
        pose.popMatrix();
    }

    private void renderConfirm(GuiGraphicsExtractor gfx, Font font, int mouseX, int mouseY) {
        float t = BhAnim.clamp01((System.currentTimeMillis() - bhConfirmOpenMs) / ENTER_MS);
        gfx.fill(0, 0, this.width, this.height, Math.round(0x99 * t) << 24);

        int cx = (this.width - CONFIRM_WIDTH) / 2;
        int cy = (this.height - CONFIRM_HEIGHT) / 2;

        var pose = gfx.pose();
        pose.pushMatrix();
        BhAnim.enter(pose, BhAnim.easeOutBack(t), cx + CONFIRM_WIDTH / 2f, cy + CONFIRM_HEIGHT / 2f, 6f, 0.9f);
        BhScreenDraw.panelTexture(gfx, cx, cy, CONFIRM_WIDTH, CONFIRM_HEIGHT, BhScreenDraw.SCREEN_CONFIRM_TEXTURE, t);

        Component name = horse.hasCustomName() ? horse.getCustomName() : ((IHorseData) horse).bh_getBreed()
                .displayName(((IHorseData) horse).bh_isMixedBreed());
        gfx.centeredText(font, Component.translatable("screen.icys-better-horses.manage.confirm_title"),
                cx + CONFIRM_WIDTH / 2, cy + 12, BhScreenDraw.TEXT);
        gfx.centeredText(font, Component.translatable("screen.icys-better-horses.manage.confirm_body", name),
                cx + CONFIRM_WIDTH / 2, cy + 26, BhScreenDraw.TEXT_MUTED);

        int btnY = confirmButtonY();
        boolean yesHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);
        boolean cancelHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);

        confirmButton(gfx, font, BhScreenDraw.DISOWN_BUTTON_SMALL_TEXTURE, confirmYesX(), btnY, "confirm_yes",
                "screen.icys-better-horses.manage.confirm_yes", DISOWN_TEXT_COLOR, yesHovered);
        confirmButton(gfx, font, BhScreenDraw.CANCEL_BUTTON_TEXTURE, confirmCancelX(), btnY, "confirm_cancel",
                "screen.icys-better-horses.manage.confirm_cancel", CANCEL_TEXT_COLOR, cancelHovered);

        pose.popMatrix();
    }

    private void confirmButton(GuiGraphicsExtractor gfx, Font font, net.minecraft.resources.Identifier texture,
                               int x, int y, Object key, String labelKey, int textColor, boolean hovered) {
        float ly = lift.get(key, hovered, LIFT_PX);
        BhScreenDraw.textureShadow(gfx, texture, x, y, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT, ly, 1f);
        var pose = gfx.pose();
        pose.pushMatrix();
        pose.translate(0f, -ly);
        BhScreenDraw.textureButton(gfx, font, texture, x, y, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT,
                Component.translatable(labelKey), textColor, 0xFFFFFFFF);
        pose.popMatrix();
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
        if (event.button() != 0 || bhClosing) {
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
            return true;
        }

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        if (BhScreenDraw.inBox(event.x(), event.y(),
                disownButtonX(left), disownButtonY(top), DISOWN_BTN_WIDTH, DISOWN_BTN_HEIGHT)) {
            press.hit("disown");
            ClientHorseRoster.clearFlash();
            confirmingDisown = true;
            bhConfirmOpenMs = System.currentTimeMillis();
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
        if (horse instanceof icy.betterhorses.net.entity.BhBreedHorse breedHorse) {
            return breedHorse.bhCoats().displayName(breedHorse.bhCoat());
        }
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
        gfx.text(font, label, x + LABEL_INDENT, y, LABEL_COLOR, false);
        gfx.text(font, value, x + LABEL_WIDTH, y, VALUE_COLOR, false);
    }

    private void drawStatRow(GuiGraphicsExtractor gfx, Font font, int x, int y, Component label, String value, double normalized) {
        gfx.text(font, label, x + LABEL_INDENT, y, LABEL_COLOR, false);
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
