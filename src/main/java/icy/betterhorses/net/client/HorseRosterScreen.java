package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseManageAction;
import icy.betterhorses.net.HorseManagement;
import icy.betterhorses.net.IcysBetterHorsesClient;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseRosterEntry;
import icy.betterhorses.net.network.OpenHorseRosterPayload;
import icy.betterhorses.net.BhNetworking;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhRegistries;
import icy.betterhorses.net.registry.BreedType;
import icy.betterhorses.net.registry.GenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import org.lwjgl.glfw.GLFW;

public class HorseRosterScreen extends Screen {

    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 200;
    private static final int PADDING = 5;
    private static final int TITLE_HEIGHT = 22;
    private static final int FOOTER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 30;
    private static final int ROW_GAP = 5;
    private static final int VISIBLE_ROWS = 5;

    private static final int ROWS_WIDTH = 300;
    private static final int PREVIEW_GAP = 5;

    private static final int SCROLL_ARROW_HEIGHT = 3;
    private static final int SCROLL_ARROW_RGB = 0x00E7B43B;

    private static final int ROW_NAME_COLOR = 0xFF3A2714;
    private static final int ROW_SUBTITLE_COLOR = 0xFF6B4A2C;
    private static final int PREVIEW_TEXT_COLOR = 0xFF3A2714;

    private static final float ENTER_MS = 240f;
    private static final float ENTER_SCALE = 0.96f;
    private static final float CARD_STAGGER_MS = 45f;
    private static final float CARD_ENTER_MS = 220f;
    private static final float CARD_RISE = 12f;
    private static final float CLOSE_MS = 150f;
    private static final float LIFT_PX = 2f;
    private static final float LIFT_TAU = 0.045f;
    private static final float PRESS_DEPTH = 0.08f;
    private static final float PRESS_MS = 130f;
    private static final float SHAKE_MS = 420f;
    private static final float SHAKE_PX = 2.5f;
    private static final float SHAKE_CYCLES = 3f;
    private static final int SELECT_ARROW_HALF = 2;

    private static final int BTN_HEIGHT = 14;
    private static final int BTN_WHISTLE_WIDTH = 50;
    private static final int BTN_HOME_WIDTH = 62;
    private static final int BTN_DISOWN_WIDTH = 15;
    private static final int BTN_DISOWN_HEIGHT = 17;
    private static final int BTN_DISOWN_FLASH_TINT = 0xFF8A2020;
    private static final float ROW_SHADOW_ALPHA = 0.6f;
    private static final int ROW_BTN_TEXT_COLOR = 0xFFEDE6DA;
    private static final int BTN_GAP = 4;
    private static final int ROW_BTN_RIGHT_PAD = 6;

    private static final int SET_ACTIVE_WIDTH = 100;
    private static final int SET_ACTIVE_HEIGHT = 22;
    private static final int SET_ACTIVE_LEFT_INSET = 4;
    private static final int SET_ACTIVE_BOTTOM_GAP = 10;
    private static final int SET_ACTIVE_TEXT_COLOR = 0xFFEDE6DA;
    private static final int ACTIVE_TEXT_COLOR = 0xFF2A210A;
    private static final int SET_ACTIVE_FLASH_TINT = 0xFFE85C5C;
    private static final int PREVIEW_TEXT_HEIGHT = 22;
    private static final int PREVIEW_LINE_HEIGHT = 10;
    private static final int PREVIEW_FRAME_BOTTOM = 136;
    private static final int PREVIEW_COORDS_Y = PREVIEW_FRAME_BOTTOM - PREVIEW_LINE_HEIGHT - 1;
    private static final int PREVIEW_TEXT_INSET = 8;
    private static final float PREVIEW_FORWARD_OFFSET = 0.0625F;
    private static final int PREVIEW_MODEL_Y_NUDGE = 5;
    private static final int PREVIEW_MIN_SCALE = 14;
    private static final int PREVIEW_MAX_SCALE = 40;
    private static final int PREVIEW_PITCH_CLAMP = 8;

    private static final int CONFIRM_WIDTH = 220;
    private static final int CONFIRM_HEIGHT = 76;
    private static final int CONFIRM_BTN_WIDTH = 84;
    private static final int CONFIRM_BTN_HEIGHT = 20;
    private static final int CONFIRM_DISOWN_TEXT_COLOR = 0xFF3A2714;
    private static final int CONFIRM_CANCEL_TEXT_COLOR = 0xFFEDE6DA;

    private int left;
    private int top;
    private int rowsTop;
    private int visibleRows;
    private int scrollOffset;

    private @Nullable UUID selectedHorseId;
    private @Nullable UUID confirmingDisownOf;

    private long bhOpenMs;
    private long bhConfirmOpenMs;
    private float bhEnter;
    private boolean bhClosing;
    private long bhCloseMs;
    private final BhAnim.Lift lift = new BhAnim.Lift();
    private final BhAnim.Press press = new BhAnim.Press();

    public HorseRosterScreen() {
        super(Component.translatable("screen.icys-better-horses.manage"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        ClientHorseRoster.clearFlash();
        BhNetworking.sendToServer(new OpenHorseRosterPayload());
        layout();
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

    private void layout() {
        List<HorseRosterEntry> entries = ClientHorseRoster.entries();
        visibleRows = VISIBLE_ROWS;
        left = (this.width - PANEL_WIDTH) / 2;
        top = (this.height - PANEL_HEIGHT) / 2;
        rowsTop = top + TITLE_HEIGHT;

        scrollOffset = Math.min(scrollOffset, Math.max(0, entries.size() - visibleRows));

        if (ClientHorseRoster.find(selectedHorseId) == null) {
            UUID active = ClientHorseRoster.activeHorseId();
            selectedHorseId = active != null ? active
                    : (entries.isEmpty() ? null : entries.get(0).horseId());
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.render(gfx, mouseX, mouseY, delta);
        layout();

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
        bhEnter = vis;
        lift.beginFrame(LIFT_TAU);

        var pose = gfx.pose();
        pose.pushPose();
        BhAnim.enter(pose, vis, left + PANEL_WIDTH / 2f, top + PANEL_HEIGHT / 2f, 0f, ENTER_SCALE);

        BhScreenDraw.panelTexture(gfx, left, top, PANEL_WIDTH, PANEL_HEIGHT, BhScreenDraw.SCREEN_MANAGE_TEXTURE, vis);
        gfx.drawCenteredString(this.font, getTitle(), left + PANEL_WIDTH / 2, top + 7, BhScreenDraw.TEXT);

        int paneMouseX = confirmingDisownOf == null ? mouseX : -1;
        int paneMouseY = confirmingDisownOf == null ? mouseY : -1;

        List<HorseRosterEntry> entries = ClientHorseRoster.entries();
        if (entries.isEmpty()) {
            gfx.drawCenteredString(this.font,
                    Component.translatable("screen.icys-better-horses.manage.empty"),
                    left + PADDING + ROWS_WIDTH / 2, rowsTop + ROW_HEIGHT / 2, BhScreenDraw.TEXT_MUTED);
        } else {
            for (int i = 0; i < visibleRows && i + scrollOffset < entries.size(); i++) {
                renderRow(gfx, entries.get(i + scrollOffset), i, rowY(i), paneMouseX, paneMouseY);
            }
        }

        renderPreview(gfx, paneMouseX, paneMouseY);

        renderFooter(gfx, entries);
        renderScrollArrows(gfx, entries);

        pose.popPose();

        if (confirmingDisownOf != null) {
            renderConfirm(gfx, mouseX, mouseY);
        }
    }

    private void renderFooter(GuiGraphics gfx, List<HorseRosterEntry> entries) {
        String flashKey = ClientHorseRoster.flashMessageKey();
        if (!flashKey.isEmpty() && !isSilentFailure(flashKey)) {
            gfx.drawCenteredString(this.font, Component.translatable(flashKey),
                    left + PADDING + ROWS_WIDTH / 2, top + PANEL_HEIGHT - FOOTER_HEIGHT + 5, BhScreenDraw.TEXT_ERROR);
        }
    }

    private static boolean isSilentFailure(String flashKey) {
        return HorseManagement.MSG_NO_HOME.equals(flashKey);
    }

    private void renderScrollArrows(GuiGraphics gfx, List<HorseRosterEntry> entries) {
        int centerX = left + PADDING + ROWS_WIDTH / 2;
        float pulse = 0.5F + 0.5F * (float) Math.sin(System.currentTimeMillis() / 380.0D);
        int alpha = (int) (0x55 + pulse * (0xFF - 0x55));
        int color = (alpha << 24) | SCROLL_ARROW_RGB;

        if (scrollOffset > 0) {
            drawArrow(gfx, centerX, rowsTop - 2 - SCROLL_ARROW_HEIGHT, true, color);
        }
        String flashKey = ClientHorseRoster.flashMessageKey();
        boolean flashing = !flashKey.isEmpty() && !isSilentFailure(flashKey);
        if (!flashing && scrollOffset + visibleRows < entries.size()) {
            int lastPlateBottom = rowY(visibleRows - 1) + ROW_HEIGHT;
            drawArrow(gfx, centerX, lastPlateBottom + 2, false, color);
        }
    }

    private void drawArrow(GuiGraphics gfx, int centerX, int topY, boolean up, int color) {
        for (int row = 0; row < SCROLL_ARROW_HEIGHT; row++) {
            int halfWidth = up ? row : (SCROLL_ARROW_HEIGHT - 1 - row);
            int y = topY + row;
            gfx.fill(centerX - halfWidth, y, centerX + halfWidth + 1, y + 1, color);
        }
    }

    private int rowY(int visibleIndex) {
        return rowsTop + visibleIndex * (ROW_HEIGHT + ROW_GAP);
    }

    private void renderRow(GuiGraphics gfx, HorseRosterEntry entry, int visibleIndex, int y, int mouseX, int mouseY) {
        int rowLeft = left + PADDING;
        boolean hovered = BhScreenDraw.inBox(mouseX, mouseY, rowLeft, y, ROWS_WIDTH, ROW_HEIGHT);
        boolean selected = entry.horseId().equals(selectedHorseId);

        float cardT = bhClosing ? bhEnter : BhAnim.easeOutCubic(
                (System.currentTimeMillis() - bhOpenMs - visibleIndex * CARD_STAGGER_MS) / CARD_ENTER_MS);
        float cardRise = (1f - cardT) * CARD_RISE;

        if (selected) {
            drawSelectArrow(gfx, rowLeft - 4, Math.round(y + ROW_HEIGHT / 2f + cardRise), BhScreenDraw.ACTIVE);
        }

        float ly = lift.get(entry.horseId(), hovered, LIFT_PX);
        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(0f, cardRise - ly, 0);

        BhScreenDraw.rowPlate(gfx, rowLeft, y, ROWS_WIDTH, ROW_HEIGHT);

        int textX = rowLeft + 6;
        if (entry.active()) {
            gfx.fill(rowLeft + 2, y + ROW_HEIGHT / 2 - 3, rowLeft + 4, y + ROW_HEIGHT / 2 + 3, BhScreenDraw.ACTIVE);
        }
        gfx.drawString(this.font, displayName(entry), textX, y + 5, ROW_NAME_COLOR, false);
        gfx.drawString(this.font, subtitle(entry), textX, y + 17, ROW_SUBTITLE_COLOR, false);

        int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        drawActionButton(gfx, entry, HorseManageAction.WHISTLE, BhScreenDraw.WHISTLE_BUTTON_TEXTURE,
                whistleButtonX(), btnY, BTN_WHISTLE_WIDTH,
                Component.translatable("screen.icys-better-horses.manage.whistle"), mouseX, mouseY);
        drawActionButton(gfx, entry, HorseManageAction.SEND_HOME, BhScreenDraw.SEND_HOME_BUTTON_TEXTURE,
                homeButtonX(), btnY, BTN_HOME_WIDTH,
                Component.translatable("screen.icys-better-horses.manage.send_home"), mouseX, mouseY);
        drawDisownPennant(gfx, entry, disownButtonX(), btnY, mouseX, mouseY);

        pose.popPose();
    }

    private void drawActionButton(GuiGraphics gfx, HorseRosterEntry entry, HorseManageAction action,
                                  ResourceLocation texture, int x, int y, int width, Component label,
                                  int mouseX, int mouseY) {
        boolean flashing = ClientHorseRoster.isFlashing(entry.horseId(), action);
        boolean hovered = BhScreenDraw.inBox(mouseX, mouseY, x, y, width, BTN_HEIGHT);
        Object key = pressKey(entry.horseId(), action);

        float shakeX = flashing ? shakeOffset(ClientHorseRoster.flashElapsedMs()) : 0f;
        float ly = lift.get(key, hovered, LIFT_PX);
        float sc = press.scale(key, PRESS_DEPTH, PRESS_MS);

        BhScreenDraw.textureShadow(gfx, texture, Math.round(x + shakeX), y, width, BTN_HEIGHT,
                ly, ROW_SHADOW_ALPHA);

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(shakeX, -ly, 0);
        if (sc != 1f) {
            float ccx = x + width / 2f;
            float ccy = y + BTN_HEIGHT / 2f;
            pose.translate(ccx, ccy, 0);
            pose.scale(sc, sc, 1);
            pose.translate(-ccx, -ccy, 0);
        }
        BhScreenDraw.textureButton(gfx, this.font, texture, x, y, width, BTN_HEIGHT,
                label, ROW_BTN_TEXT_COLOR, 0xFFFFFFFF);
        if (flashing) {
            BhScreenDraw.errorWash(gfx, x, y, width, BTN_HEIGHT);
        }
        pose.popPose();
    }

    private void drawDisownPennant(GuiGraphics gfx, HorseRosterEntry entry,
                                   int x, int y, int mouseX, int mouseY) {
        boolean flashing = ClientHorseRoster.isFlashing(entry.horseId(), HorseManageAction.DISOWN);
        boolean hovered = BhScreenDraw.inBox(mouseX, mouseY, x, y, BTN_DISOWN_WIDTH, BTN_DISOWN_HEIGHT);
        int tint = flashing ? BTN_DISOWN_FLASH_TINT : 0xFFFFFFFF;

        float shakeX = flashing ? shakeOffset(ClientHorseRoster.flashElapsedMs()) : 0f;
        float ly = lift.get(pressKey(entry.horseId(), HorseManageAction.DISOWN), hovered, LIFT_PX);
        float sc = press.scale(pressKey(entry.horseId(), HorseManageAction.DISOWN), PRESS_DEPTH, PRESS_MS);

        BhScreenDraw.textureShadow(gfx, BhScreenDraw.CROSS_BUTTON_TEXTURE, Math.round(x + shakeX), y,
                BTN_DISOWN_WIDTH, BTN_DISOWN_HEIGHT, ly, ROW_SHADOW_ALPHA);

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(shakeX, -ly, 0);
        if (sc != 1f) {
            float ccx = x + BTN_DISOWN_WIDTH / 2f;
            pose.translate(ccx, y, 0);
            pose.scale(sc, sc, 1);
            pose.translate(-ccx, -y, 0);
        }
        gfx.setColor(((tint >> 16) & 255) / 255f, ((tint >> 8) & 255) / 255f,
                (tint & 255) / 255f, ((tint >>> 24) & 255) / 255f);
        gfx.blit(BhScreenDraw.CROSS_BUTTON_TEXTURE, x, y, 0.0F, 0.0F,
                BTN_DISOWN_WIDTH, BTN_DISOWN_HEIGHT, BTN_DISOWN_WIDTH, BTN_DISOWN_HEIGHT);
        gfx.setColor(1f, 1f, 1f, 1f);
        pose.popPose();
    }

    private static float shakeOffset(long elapsedMs) {
        if (elapsedMs < 0L || elapsedMs >= SHAKE_MS) return 0f;
        float t = elapsedMs / SHAKE_MS;
        return (float) (Math.sin(t * Math.PI * 2 * SHAKE_CYCLES) * SHAKE_PX * (1f - t));
    }

    private static String pressKey(UUID horseId, HorseManageAction action) {
        return horseId + "#" + action.ordinal();
    }

    private void drawSelectArrow(GuiGraphics gfx, int baseLeftX, int centerY, int color) {
        for (int r = -SELECT_ARROW_HALF; r <= SELECT_ARROW_HALF; r++) {
            int len = (SELECT_ARROW_HALF - Math.abs(r)) + 1;
            gfx.fill(baseLeftX, centerY + r, baseLeftX + len, centerY + r + 1, color);
        }
    }

    private int disownButtonX() {
        return left + PADDING + ROWS_WIDTH - ROW_BTN_RIGHT_PAD - BTN_DISOWN_WIDTH;
    }

    private int homeButtonX() {
        return disownButtonX() - BTN_GAP - BTN_HOME_WIDTH;
    }

    private int whistleButtonX() {
        return homeButtonX() - BTN_GAP - BTN_WHISTLE_WIDTH;
    }

    private int previewX() {
        return left + PADDING + ROWS_WIDTH + PREVIEW_GAP + PREVIEW_GAP + 1;
    }

    private int previewWidth() {
        return left + PANEL_WIDTH - PADDING - previewX();
    }

    private int setActiveButtonY() {
        return top + PANEL_HEIGHT - SET_ACTIVE_BOTTOM_GAP - SET_ACTIVE_HEIGHT;
    }

    private int setActiveButtonX() {
        return previewX() + SET_ACTIVE_LEFT_INSET;
    }

    private void renderPreview(GuiGraphics gfx, int mouseX, int mouseY) {
        int x = previewX();
        int width = previewWidth();
        HorseRosterEntry selected = ClientHorseRoster.find(selectedHorseId);

        int modelTop = rowsTop + 4;
        int coordsY = top + PREVIEW_COORDS_Y;
        int modelBottom = coordsY - 2;

        if (selected == null) {
            inkCentered(gfx, Component.translatable("screen.icys-better-horses.manage.preview_empty"),
                    x + width / 2, (modelTop + modelBottom) / 2);
            return;
        }

        boolean settled = bhEnter > 0.9f;
        AbstractHorse preview = HorsePreviewCache.getOrBuild(selected);
        boolean drawn = false;
        if (settled && preview != null) {
            try {
                renderPreviewModel(gfx, preview, x, modelTop, x + width, modelBottom, mouseX, mouseY);
                drawn = true;
            } catch (Exception e) {
                HorsePreviewCache.markBroken(selected.horseId(), e);
            }
        }
        if (settled && !drawn) {
            inkCentered(gfx, Component.translatable("screen.icys-better-horses.manage.preview_missing"),
                    x + width / 2, (modelTop + modelBottom) / 2);
        }

        int centerX = x + width / 2;
        BlockPos pos = currentPos(selected);
        inkCenteredFitted(gfx, Component.translatable("screen.icys-better-horses.manage.coords",
                pos.getX(), pos.getY(), pos.getZ()), centerX, coordsY, width - PREVIEW_TEXT_INSET);

        int line = setActiveButtonY() - PREVIEW_TEXT_HEIGHT;
        inkCentered(gfx, Component.translatable("screen.icys-better-horses.manage.bond", selected.bond()),
                centerX, line);
        line += PREVIEW_LINE_HEIGHT;
        inkCentered(gfx, selected.hasHome()
                        ? Component.translatable("screen.icys-better-horses.manage.has_home")
                        : Component.translatable("screen.icys-better-horses.manage.no_home"),
                centerX, line);

        renderSetActiveButton(gfx, selected, x, width, mouseX, mouseY);
    }

    private static BlockPos currentPos(HorseRosterEntry entry) {
        Minecraft minecraft = Minecraft.getInstance();
        if (entry.loaded() && minecraft.level != null) {
            for (Entity live : minecraft.level.entitiesForRendering()) {
                if (live.getUUID().equals(entry.horseId())) {
                    return live.blockPosition();
                }
            }
        }
        return entry.pos();
    }

    private void inkCentered(GuiGraphics gfx, Component text, int centerX, int y) {
        gfx.drawString(this.font, text, centerX - this.font.width(text) / 2, y, PREVIEW_TEXT_COLOR, false);
    }

    private void inkCenteredFitted(GuiGraphics gfx, Component text, int centerX, int y, int maxWidth) {
        int textWidth = this.font.width(text);
        if (textWidth <= maxWidth || maxWidth <= 0) {
            inkCentered(gfx, text, centerX, y);
            return;
        }

        float scale = maxWidth / (float) textWidth;
        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(centerX, (float) y, 0);
        pose.scale(scale, scale, 1);
        gfx.drawString(this.font, text, -textWidth / 2, 0, PREVIEW_TEXT_COLOR, false);
        pose.popPose();
    }

    private void renderPreviewModel(GuiGraphics gfx, AbstractHorse preview,
                                    int x0, int y0, int x1, int y1, int mouseX, int mouseY) {
        y0 += PREVIEW_MODEL_Y_NUDGE;
        y1 += PREVIEW_MODEL_Y_NUDGE;

        float boxWidth = Math.max(0.1F, preview.getBbWidth());
        float boxHeight = Math.max(0.1F, preview.getBbHeight());
        int scaleFromHeight = (int) ((y1 - y0) * 0.55F / boxHeight);
        int scaleFromWidth = (int) ((x1 - x0) * 0.6F / boxWidth);
        int scale = Math.max(PREVIEW_MIN_SCALE,
                Math.min(PREVIEW_MAX_SCALE, Math.min(scaleFromHeight, scaleFromWidth)));

        int verticalCenter = (y0 + y1) / 2;
        int clampedMouseY = Math.max(verticalCenter - PREVIEW_PITCH_CLAMP,
                Math.min(verticalCenter + PREVIEW_PITCH_CLAMP, mouseY));

        int centerX = (x0 + x1) / 2;
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                gfx, centerX, y1, scale,
                centerX - mouseX, verticalCenter - clampedMouseY, preview);
    }

    private void renderSetActiveButton(GuiGraphics gfx, HorseRosterEntry selected,
                                       int x, int width, int mouseX, int mouseY) {
        int btnX = setActiveButtonX();
        int btnY = setActiveButtonY();

        boolean alreadyActive = selected.active();
        boolean hovered = !alreadyActive
                && BhScreenDraw.inBox(mouseX, mouseY, btnX, btnY, SET_ACTIVE_WIDTH, SET_ACTIVE_HEIGHT);
        boolean flashing = ClientHorseRoster.isFlashing(selected.horseId(), HorseManageAction.SET_ACTIVE);

        ResourceLocation texture = alreadyActive
                ? BhScreenDraw.ACTIVE_BUTTON_TEXTURE
                : BhScreenDraw.SET_ACTIVE_BUTTON_TEXTURE;
        Component label = alreadyActive
                ? Component.translatable("screen.icys-better-horses.manage.is_active")
                : Component.translatable("screen.icys-better-horses.manage.set_active");
        int textColor = alreadyActive ? ACTIVE_TEXT_COLOR : SET_ACTIVE_TEXT_COLOR;
        int tint = flashing ? SET_ACTIVE_FLASH_TINT : 0xFFFFFFFF;

        float shakeX = flashing ? shakeOffset(ClientHorseRoster.flashElapsedMs()) : 0f;
        float ly = lift.get("setActive", hovered, LIFT_PX);
        float sc = press.scale("setActive", PRESS_DEPTH, PRESS_MS);

        BhScreenDraw.textureShadow(gfx, texture, Math.round(btnX + shakeX), btnY,
                SET_ACTIVE_WIDTH, SET_ACTIVE_HEIGHT, ly, 1f);

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(shakeX, -ly, 0);
        if (sc != 1f) {
            float ccx = btnX + SET_ACTIVE_WIDTH / 2f;
            float ccy = btnY + SET_ACTIVE_HEIGHT / 2f;
            pose.translate(ccx, ccy, 0);
            pose.scale(sc, sc, 1);
            pose.translate(-ccx, -ccy, 0);
        }
        BhScreenDraw.textureButton(gfx, this.font, texture, btnX, btnY, SET_ACTIVE_WIDTH, SET_ACTIVE_HEIGHT,
                label, textColor, tint);
        pose.popPose();
    }

    private void renderConfirm(GuiGraphics gfx, int mouseX, int mouseY) {
        float t = BhAnim.clamp01((System.currentTimeMillis() - bhConfirmOpenMs) / ENTER_MS);
        gfx.fill(0, 0, this.width, this.height, Math.round(0x99 * t) << 24);

        int cx = (this.width - CONFIRM_WIDTH) / 2;
        int cy = (this.height - CONFIRM_HEIGHT) / 2;

        var pose = gfx.pose();
        pose.pushPose();
        BhAnim.enter(pose, BhAnim.easeOutBack(t), cx + CONFIRM_WIDTH / 2f, cy + CONFIRM_HEIGHT / 2f, 6f, 0.9f);
        BhScreenDraw.panelTexture(gfx, cx, cy, CONFIRM_WIDTH, CONFIRM_HEIGHT, BhScreenDraw.SCREEN_CONFIRM_TEXTURE, t);

        HorseRosterEntry entry = ClientHorseRoster.find(confirmingDisownOf);
        Component name = entry == null ? Component.literal("?") : displayName(entry);
        gfx.drawCenteredString(this.font, Component.translatable("screen.icys-better-horses.manage.confirm_title"),
                cx + CONFIRM_WIDTH / 2, cy + 12, BhScreenDraw.TEXT);
        gfx.drawCenteredString(this.font, Component.translatable("screen.icys-better-horses.manage.confirm_body", name),
                cx + CONFIRM_WIDTH / 2, cy + 26, BhScreenDraw.TEXT_MUTED);

        int btnY = confirmButtonY();
        boolean yesHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);
        boolean cancelHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);

        confirmButton(gfx, BhScreenDraw.DISOWN_BUTTON_SMALL_TEXTURE, confirmYesX(), btnY, "confirm_yes",
                "screen.icys-better-horses.manage.confirm_yes", CONFIRM_DISOWN_TEXT_COLOR, yesHovered);
        confirmButton(gfx, BhScreenDraw.CANCEL_BUTTON_TEXTURE, confirmCancelX(), btnY, "confirm_cancel",
                "screen.icys-better-horses.manage.confirm_cancel", CONFIRM_CANCEL_TEXT_COLOR, cancelHovered);

        pose.popPose();
    }

    private void confirmButton(GuiGraphics gfx, ResourceLocation texture,
                               int x, int y, Object key, String labelKey, int textColor, boolean hovered) {
        float ly = lift.get(key, hovered, LIFT_PX);
        BhScreenDraw.textureShadow(gfx, texture, x, y, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT, ly, 1f);
        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(0f, -ly, 0);
        BhScreenDraw.textureButton(gfx, this.font, texture, x, y, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT,
                Component.translatable(labelKey), textColor, 0xFFFFFFFF);
        pose.popPose();
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

    private Component displayName(HorseRosterEntry entry) {
        if (!entry.customName().isEmpty()) {
            return Component.literal(entry.customName());
        }
        return breedDisplayName(entry.breedId(), entry.mixedBreed());
    }

    private static Component breedDisplayName(String breedId, boolean mixed) {
        ResourceLocation location = breedId.indexOf(':') >= 0 ? ResourceLocation.tryParse(breedId) : null;
        if (location != null && BhRegistries.breedTypeRegistry().containsKey(location)) {
            ResourceKey<BreedType> key = ResourceKey.create(BhRegistries.BREED_TYPES, location);
            return BreedType.displayName(key, mixed);
        }
        return HorseBreed.byId(breedId).displayName(mixed);
    }

    private Component subtitle(HorseRosterEntry entry) {
        Component where = entry.loaded()
                ? Component.literal(BhScreenDraw.prettifyDimension(entry.dimensionId()))
                : Component.translatable("screen.icys-better-horses.manage.resting");
        return Component.empty()
                .append(GenderType.displayName(bh_genderKey(entry.genderId())))
                .append(" · ")
                .append(where);
    }

    private static ResourceKey<GenderType> bh_genderKey(String genderId) {
        ResourceLocation loc = ResourceLocation.tryParse(genderId);
        return loc != null ? ResourceKey.create(BhRegistries.GENDER_TYPES, loc) : BhContent.MALE.getKey();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || bhClosing) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (confirmingDisownOf != null) {
            int btnY = confirmButtonY();
            if (BhScreenDraw.inBox(mouseX, mouseY, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT)) {
                send(confirmingDisownOf, HorseManageAction.DISOWN);
                confirmingDisownOf = null;
                return true;
            }
            if (BhScreenDraw.inBox(mouseX, mouseY, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT)) {
                confirmingDisownOf = null;
                return true;
            }
            return true;
        }

        HorseRosterEntry selected = ClientHorseRoster.find(selectedHorseId);
        if (selected != null && !selected.active()
                && BhScreenDraw.inBox(mouseX, mouseY, setActiveButtonX(), setActiveButtonY(),
                        SET_ACTIVE_WIDTH, SET_ACTIVE_HEIGHT)) {
            press.hit("setActive");
            send(selected.horseId(), HorseManageAction.SET_ACTIVE);
            return true;
        }

        List<HorseRosterEntry> entries = ClientHorseRoster.entries();
        for (int i = 0; i < visibleRows && i + scrollOffset < entries.size(); i++) {
            HorseRosterEntry entry = entries.get(i + scrollOffset);
            int y = rowY(i);
            int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;

            if (BhScreenDraw.inBox(mouseX, mouseY, whistleButtonX(), btnY, BTN_WHISTLE_WIDTH, BTN_HEIGHT)) {
                press.hit(pressKey(entry.horseId(), HorseManageAction.WHISTLE));
                send(entry.horseId(), HorseManageAction.WHISTLE);
                return true;
            }
            if (BhScreenDraw.inBox(mouseX, mouseY, homeButtonX(), btnY, BTN_HOME_WIDTH, BTN_HEIGHT)) {
                press.hit(pressKey(entry.horseId(), HorseManageAction.SEND_HOME));
                send(entry.horseId(), HorseManageAction.SEND_HOME);
                return true;
            }
            if (BhScreenDraw.inBox(mouseX, mouseY, disownButtonX(), btnY, BTN_DISOWN_WIDTH, BTN_DISOWN_HEIGHT)) {
                press.hit(pressKey(entry.horseId(), HorseManageAction.DISOWN));
                ClientHorseRoster.clearFlash();
                confirmingDisownOf = entry.horseId();
                bhConfirmOpenMs = System.currentTimeMillis();
                return true;
            }
            if (BhScreenDraw.inBox(mouseX, mouseY, left + PADDING, y, ROWS_WIDTH, ROW_HEIGHT)) {
                selectedHorseId = entry.horseId();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void send(UUID horseId, HorseManageAction action) {
        ClientHorseRoster.clearFlash();
        BhNetworking.sendToServer(new HorseManagePayload(horseId, action.ordinal()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (confirmingDisownOf != null) return true;
        int maxOffset = Math.max(0, ClientHorseRoster.entries().size() - visibleRows);
        if (scroll > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scroll < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (confirmingDisownOf != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                confirmingDisownOf = null;
            }
            return true;
        }
        if (IcysBetterHorsesClient.MANAGE_KEY != null && IcysBetterHorsesClient.MANAGE_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        ClientHorseRoster.clearFlash();
    }
}
