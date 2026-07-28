package icy.betterhorses.net.client;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.HorseManageAction;
import icy.betterhorses.net.IcysBetterHorsesClient;
import icy.betterhorses.net.network.HorseManagePayload;
import icy.betterhorses.net.network.HorseRosterEntry;
import icy.betterhorses.net.network.OpenHorseRosterPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * The horse management screen: every horse the player owns, loaded or resting in an unloaded chunk,
 * with a whistle / send-home / disown button each, plus a preview of the selected horse and the
 * "set active" toggle that decides which horse the whistle keybind calls.
 *
 * <p>The server owns every decision. A button press is a request; the reply either refreshes the
 * roster (success) or flashes that one button red with a reason (no home set, still carrying gear).
 * Disowning goes through a confirmation panel first, and the equipment check happens after the
 * player confirms — so a mistake costs a click, not a horse.</p>
 */
public class HorseRosterScreen extends Screen {

    private static final int PANEL_WIDTH = 440;
    /** Fixed so a single background image always fits; the list scrolls inside it. */
    private static final int PANEL_HEIGHT = 200;
    private static final int PADDING = 5;
    private static final int TITLE_HEIGHT = 22;
    private static final int FOOTER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 30;
    private static final int ROW_GAP = 2;
    /** How many rows fit the fixed panel: (PANEL_HEIGHT - TITLE_HEIGHT - FOOTER_HEIGHT) / (ROW_HEIGHT + ROW_GAP). */
    private static final int VISIBLE_ROWS = 5;

    private static final int ROWS_WIDTH = 300;
    private static final int PREVIEW_GAP = 5;

    private static final int SCROLLBAR_WIDTH = 3;
    private static final int SCROLLBAR_MIN_THUMB = 12;

    private static final int BTN_HEIGHT = 14;
    private static final int BTN_WHISTLE_WIDTH = 50;
    private static final int BTN_HOME_WIDTH = 62;
    private static final int BTN_DISOWN_WIDTH = 14;
    private static final int BTN_GAP = 4;
    private static final int ROW_BTN_RIGHT_PAD = 6;

    private static final int SET_ACTIVE_HEIGHT = 16;
    private static final int PREVIEW_TEXT_HEIGHT = 22;
    private static final float PREVIEW_FORWARD_OFFSET = 0.0625F;
    private static final int PREVIEW_MIN_SCALE = 14;
    private static final int PREVIEW_MAX_SCALE = 40;
    private static final int PREVIEW_PITCH_CLAMP = 8;

    private static final int CONFIRM_WIDTH = 220;
    private static final int CONFIRM_HEIGHT = 76;
    private static final int CONFIRM_BTN_WIDTH = 84;
    private static final int CONFIRM_BTN_HEIGHT = 18;

    private int left;
    private int top;
    private int rowsTop;
    private int visibleRows;
    private int scrollOffset;

    private @Nullable UUID selectedHorseId;
    /** Non-null while the "are you sure?" panel is up for that horse. */
    private @Nullable UUID confirmingDisownOf;

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
        ClientPlayNetworking.send(new OpenHorseRosterPayload());
        layout();
    }

    private void layout() {
        List<HorseRosterEntry> entries = ClientHorseRoster.entries();
        visibleRows = VISIBLE_ROWS;
        left = (this.width - PANEL_WIDTH) / 2;
        top = (this.height - PANEL_HEIGHT) / 2;
        rowsTop = top + TITLE_HEIGHT;

        scrollOffset = Math.min(scrollOffset, Math.max(0, entries.size() - visibleRows));

        // Selection defaults to the active horse, then the first row, and survives roster refreshes.
        if (ClientHorseRoster.find(selectedHorseId) == null) {
            UUID active = ClientHorseRoster.activeHorseId();
            selectedHorseId = active != null ? active
                    : (entries.isEmpty() ? null : entries.get(0).horseId());
        }
    }

    // ------------------------------------------------------------------ render

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(gfx, mouseX, mouseY, delta);
        layout();

        BhScreenDraw.panel(gfx, left, top, PANEL_WIDTH, PANEL_HEIGHT);
        gfx.centeredText(this.font, getTitle(), left + PANEL_WIDTH / 2, top + 7, BhScreenDraw.TEXT);

        // The confirm panel is modal, so everything underneath stops reacting to the cursor.
        int paneMouseX = confirmingDisownOf == null ? mouseX : -1;
        int paneMouseY = confirmingDisownOf == null ? mouseY : -1;

        List<HorseRosterEntry> entries = ClientHorseRoster.entries();
        if (entries.isEmpty()) {
            gfx.centeredText(this.font,
                    Component.translatable("screen.icys-better-horses.manage.empty"),
                    left + PADDING + ROWS_WIDTH / 2, rowsTop + ROW_HEIGHT / 2, BhScreenDraw.TEXT_MUTED);
        } else {
            for (int i = 0; i < visibleRows && i + scrollOffset < entries.size(); i++) {
                renderRow(gfx, entries.get(i + scrollOffset), rowY(i), paneMouseX, paneMouseY);
            }
        }

        renderScrollbar(gfx, entries.size());

        int separatorX = left + PADDING + ROWS_WIDTH + PREVIEW_GAP;
        gfx.fill(separatorX, rowsTop, separatorX + 1, top + PANEL_HEIGHT - PADDING, BhScreenDraw.PANEL_BORDER);
        renderPreview(gfx, paneMouseX, paneMouseY);

        renderFooter(gfx, entries);

        if (confirmingDisownOf != null) {
            renderConfirm(gfx, mouseX, mouseY);
        }
    }

    private void renderFooter(GuiGraphicsExtractor gfx, List<HorseRosterEntry> entries) {
        int footerY = top + PANEL_HEIGHT - FOOTER_HEIGHT + 5;
        String flashKey = ClientHorseRoster.flashMessageKey();
        if (!flashKey.isEmpty()) {
            gfx.centeredText(this.font, Component.translatable(flashKey),
                    left + PADDING + ROWS_WIDTH / 2, footerY, BhScreenDraw.TEXT_ERROR);
            return;
        }

        int hidden = Math.max(0, entries.size() - visibleRows);
        if (hidden > 0) {
            gfx.centeredText(this.font,
                    Component.translatable("screen.icys-better-horses.manage.scroll_hint", hidden),
                    left + PADDING + ROWS_WIDTH / 2, footerY, BhScreenDraw.TEXT_MUTED);
        }
    }

    /** A slim indicator down the right edge of the list so it's obvious there are more horses to scroll to. */
    private void renderScrollbar(GuiGraphicsExtractor gfx, int totalEntries) {
        if (totalEntries <= visibleRows) {
            return; // everything already fits — no scrollbar needed
        }
        int trackX = left + PADDING + ROWS_WIDTH + 1;
        int trackHeight = visibleRows * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
        gfx.fill(trackX, rowsTop, trackX + SCROLLBAR_WIDTH, rowsTop + trackHeight, BhScreenDraw.PANEL_BORDER);

        int maxOffset = totalEntries - visibleRows;
        int thumbHeight = Math.max(SCROLLBAR_MIN_THUMB, trackHeight * visibleRows / totalEntries);
        int thumbY = rowsTop + (trackHeight - thumbHeight) * scrollOffset / maxOffset;
        gfx.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, BhScreenDraw.BTN_GREY_HOVER);
    }

    private int rowY(int visibleIndex) {
        return rowsTop + visibleIndex * (ROW_HEIGHT + ROW_GAP);
    }

    private void renderRow(GuiGraphicsExtractor gfx, HorseRosterEntry entry, int y, int mouseX, int mouseY) {
        int rowLeft = left + PADDING;
        boolean hovered = BhScreenDraw.inBox(mouseX, mouseY, rowLeft, y, ROWS_WIDTH, ROW_HEIGHT);
        boolean selected = entry.horseId().equals(selectedHorseId);
        int background = selected
                ? BhScreenDraw.ROW_SELECTED
                : (hovered ? BhScreenDraw.ROW_HOVER : BhScreenDraw.ROW_BG);
        gfx.fill(rowLeft, y, rowLeft + ROWS_WIDTH, y + ROW_HEIGHT, background);

        int textX = rowLeft + 6;
        if (entry.active()) {
            // Small marker so the whistle target is obvious without opening the preview.
            gfx.fill(rowLeft + 2, y + ROW_HEIGHT / 2 - 3, rowLeft + 4, y + ROW_HEIGHT / 2 + 3, BhScreenDraw.ACTIVE);
        }
        gfx.text(this.font, displayName(entry), textX, y + 5, BhScreenDraw.TEXT, false);
        gfx.text(this.font, subtitle(entry), textX, y + 17, BhScreenDraw.TEXT_MUTED, false);

        int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;
        drawActionButton(gfx, entry, HorseManageAction.WHISTLE, whistleButtonX(), btnY, BTN_WHISTLE_WIDTH,
                Component.translatable("screen.icys-better-horses.manage.whistle"),
                BhScreenDraw.BTN_WHISTLE, BhScreenDraw.BTN_WHISTLE_HOVER, mouseX, mouseY);
        drawActionButton(gfx, entry, HorseManageAction.SEND_HOME, homeButtonX(), btnY, BTN_HOME_WIDTH,
                Component.translatable("screen.icys-better-horses.manage.send_home"),
                BhScreenDraw.BTN_HOME, BhScreenDraw.BTN_HOME_HOVER, mouseX, mouseY);
        drawActionButton(gfx, entry, HorseManageAction.DISOWN, disownButtonX(), btnY, BTN_DISOWN_WIDTH,
                Component.literal("X"),
                BhScreenDraw.BTN_DISOWN, BhScreenDraw.BTN_DISOWN_HOVER, mouseX, mouseY);
    }

    private void drawActionButton(GuiGraphicsExtractor gfx, HorseRosterEntry entry, HorseManageAction action,
                                  int x, int y, int width, Component label, int color, int hoverColor,
                                  int mouseX, int mouseY) {
        int drawColor;
        if (ClientHorseRoster.isFlashing(entry.horseId(), action)) {
            drawColor = BhScreenDraw.BTN_ERROR;
        } else {
            drawColor = BhScreenDraw.inBox(mouseX, mouseY, x, y, width, BTN_HEIGHT) ? hoverColor : color;
        }
        BhScreenDraw.button(gfx, this.font, x, y, width, BTN_HEIGHT, label, drawColor, BhScreenDraw.TEXT);
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

    // ----------------------------------------------------------------- preview

    private int previewX() {
        return left + PADDING + ROWS_WIDTH + PREVIEW_GAP + PREVIEW_GAP + 1;
    }

    private int previewWidth() {
        return left + PANEL_WIDTH - PADDING - previewX();
    }

    private int setActiveButtonY() {
        return top + PANEL_HEIGHT - PADDING - SET_ACTIVE_HEIGHT;
    }

    private void renderPreview(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        int x = previewX();
        int width = previewWidth();
        HorseRosterEntry selected = ClientHorseRoster.find(selectedHorseId);

        int modelTop = rowsTop + 4;
        int modelBottom = setActiveButtonY() - PREVIEW_TEXT_HEIGHT - 4;

        if (selected == null) {
            gfx.centeredText(this.font, Component.translatable("screen.icys-better-horses.manage.preview_empty"),
                    x + width / 2, (modelTop + modelBottom) / 2, BhScreenDraw.TEXT_MUTED);
            return;
        }

        AbstractHorse preview = HorsePreviewCache.getOrBuild(selected);
        boolean drawn = false;
        if (preview != null) {
            try {
                renderPreviewModel(gfx, preview, x, modelTop, x + width, modelBottom, mouseX, mouseY);
                drawn = true;
            } catch (Exception e) {
                HorsePreviewCache.markBroken(selected.horseId(), e);
            }
        }
        if (!drawn) {
            gfx.centeredText(this.font, Component.translatable("screen.icys-better-horses.manage.preview_missing"),
                    x + width / 2, (modelTop + modelBottom) / 2, BhScreenDraw.TEXT_MUTED);
        }

        // The details the rows no longer have room for.
        gfx.centeredText(this.font,
                Component.translatable("screen.icys-better-horses.manage.bond", selected.bond()),
                x + width / 2, modelBottom + 4, BhScreenDraw.TEXT_MUTED);
        gfx.centeredText(this.font,
                selected.hasHome()
                        ? Component.translatable("screen.icys-better-horses.manage.has_home")
                        : Component.translatable("screen.icys-better-horses.manage.no_home"),
                x + width / 2, modelBottom + 14, BhScreenDraw.TEXT_MUTED);

        renderSetActiveButton(gfx, selected, x, width, mouseX, mouseY);
    }

    private void renderPreviewModel(GuiGraphicsExtractor gfx, AbstractHorse preview,
                                    int x0, int y0, int x1, int y1, int mouseX, int mouseY) {
        float boxWidth = Math.max(0.1F, preview.getBbWidth());
        float boxHeight = Math.max(0.1F, preview.getBbHeight());
        int scaleFromHeight = (int) ((y1 - y0) * 0.55F / boxHeight);
        int scaleFromWidth = (int) ((x1 - x0) * 0.6F / boxWidth);
        int scale = Math.max(PREVIEW_MIN_SCALE,
                Math.min(PREVIEW_MAX_SCALE, Math.min(scaleFromHeight, scaleFromWidth)));

        // Let the horse follow the cursor horizontally, but keep the pitch nearly level.
        int verticalCenter = (y0 + y1) / 2;
        int clampedMouseY = Math.max(verticalCenter - PREVIEW_PITCH_CLAMP,
                Math.min(verticalCenter + PREVIEW_PITCH_CLAMP, mouseY));

        InventoryScreen.extractEntityInInventoryFollowsMouse(
                gfx, x0, y0, x1, y1, scale, PREVIEW_FORWARD_OFFSET, mouseX, clampedMouseY, preview);
    }

    private void renderSetActiveButton(GuiGraphicsExtractor gfx, HorseRosterEntry selected,
                                       int x, int width, int mouseX, int mouseY) {
        int btnX = x + 2;
        int btnWidth = width - 4;
        int btnY = setActiveButtonY();

        boolean alreadyActive = selected.active();
        boolean hovered = !alreadyActive && BhScreenDraw.inBox(mouseX, mouseY, btnX, btnY, btnWidth, SET_ACTIVE_HEIGHT);
        int color;
        if (ClientHorseRoster.isFlashing(selected.horseId(), HorseManageAction.SET_ACTIVE)) {
            color = BhScreenDraw.BTN_ERROR;
        } else if (alreadyActive) {
            color = BhScreenDraw.ACTIVE;
        } else {
            color = hovered ? BhScreenDraw.BTN_GREY_HOVER : BhScreenDraw.BTN_GREY;
        }
        Component label = alreadyActive
                ? Component.translatable("screen.icys-better-horses.manage.is_active")
                : Component.translatable("screen.icys-better-horses.manage.set_active");
        BhScreenDraw.button(gfx, this.font, btnX, btnY, btnWidth, SET_ACTIVE_HEIGHT, label, color,
                alreadyActive ? BhScreenDraw.TEXT_ON_ACTIVE : BhScreenDraw.TEXT);
    }

    // ------------------------------------------------------------ confirm panel

    private void renderConfirm(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        gfx.fill(0, 0, this.width, this.height, 0x99000000);

        int cx = (this.width - CONFIRM_WIDTH) / 2;
        int cy = (this.height - CONFIRM_HEIGHT) / 2;
        BhScreenDraw.panel(gfx, cx, cy, CONFIRM_WIDTH, CONFIRM_HEIGHT);

        HorseRosterEntry entry = ClientHorseRoster.find(confirmingDisownOf);
        Component name = entry == null ? Component.literal("?") : displayName(entry);
        gfx.centeredText(this.font, Component.translatable("screen.icys-better-horses.manage.confirm_title"),
                cx + CONFIRM_WIDTH / 2, cy + 12, BhScreenDraw.TEXT);
        gfx.centeredText(this.font, Component.translatable("screen.icys-better-horses.manage.confirm_body", name),
                cx + CONFIRM_WIDTH / 2, cy + 26, BhScreenDraw.TEXT_MUTED);

        int btnY = confirmButtonY();
        boolean yesHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);
        boolean cancelHovered = BhScreenDraw.inBox(mouseX, mouseY, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT);

        BhScreenDraw.button(gfx, this.font, confirmYesX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT,
                Component.translatable("screen.icys-better-horses.manage.confirm_yes"),
                yesHovered ? BhScreenDraw.BTN_DISOWN_HOVER : BhScreenDraw.BTN_DISOWN, BhScreenDraw.TEXT);
        BhScreenDraw.button(gfx, this.font, confirmCancelX(), btnY, CONFIRM_BTN_WIDTH, CONFIRM_BTN_HEIGHT,
                Component.translatable("screen.icys-better-horses.manage.confirm_cancel"),
                cancelHovered ? BhScreenDraw.BTN_NEUTRAL_HOVER : BhScreenDraw.BTN_NEUTRAL, BhScreenDraw.TEXT);
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

    // ------------------------------------------------------------------ labels

    private Component displayName(HorseRosterEntry entry) {
        if (!entry.customName().isEmpty()) {
            return Component.literal(entry.customName());
        }
        return HorseBreed.fromId(entry.breedOrdinal()).displayName(entry.mixedBreed());
    }

    private Component subtitle(HorseRosterEntry entry) {
        Component where = entry.loaded()
                ? Component.literal(BhScreenDraw.prettifyDimension(entry.dimensionId()))
                : Component.translatable("screen.icys-better-horses.manage.resting");
        return Component.empty()
                .append(HorseGender.fromId(entry.genderOrdinal()).displayName())
                .append(" · ")
                .append(where);
    }

    // ------------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }
        double mouseX = event.x();
        double mouseY = event.y();

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
            // Modal: swallow everything else so the rows behind it can't be clicked.
            return true;
        }

        HorseRosterEntry selected = ClientHorseRoster.find(selectedHorseId);
        if (selected != null && !selected.active()
                && BhScreenDraw.inBox(mouseX, mouseY, previewX() + 2, setActiveButtonY(),
                        previewWidth() - 4, SET_ACTIVE_HEIGHT)) {
            send(selected.horseId(), HorseManageAction.SET_ACTIVE);
            return true;
        }

        List<HorseRosterEntry> entries = ClientHorseRoster.entries();
        for (int i = 0; i < visibleRows && i + scrollOffset < entries.size(); i++) {
            HorseRosterEntry entry = entries.get(i + scrollOffset);
            int y = rowY(i);
            int btnY = y + (ROW_HEIGHT - BTN_HEIGHT) / 2;

            if (BhScreenDraw.inBox(mouseX, mouseY, whistleButtonX(), btnY, BTN_WHISTLE_WIDTH, BTN_HEIGHT)) {
                send(entry.horseId(), HorseManageAction.WHISTLE);
                return true;
            }
            if (BhScreenDraw.inBox(mouseX, mouseY, homeButtonX(), btnY, BTN_HOME_WIDTH, BTN_HEIGHT)) {
                send(entry.horseId(), HorseManageAction.SEND_HOME);
                return true;
            }
            if (BhScreenDraw.inBox(mouseX, mouseY, disownButtonX(), btnY, BTN_DISOWN_WIDTH, BTN_HEIGHT)) {
                ClientHorseRoster.clearFlash();
                confirmingDisownOf = entry.horseId();
                return true;
            }
            // Anywhere else on the row just selects it for the preview.
            if (BhScreenDraw.inBox(mouseX, mouseY, left + PADDING, y, ROWS_WIDTH, ROW_HEIGHT)) {
                selectedHorseId = entry.horseId();
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void send(UUID horseId, HorseManageAction action) {
        ClientHorseRoster.clearFlash();
        ClientPlayNetworking.send(new HorseManagePayload(horseId, action.ordinal()));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (confirmingDisownOf != null) return true;
        int maxOffset = Math.max(0, ClientHorseRoster.entries().size() - visibleRows);
        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxOffset, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (confirmingDisownOf != null) {
            // ESC backs out of the confirmation rather than closing the whole screen.
            if (event.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                confirmingDisownOf = null;
            }
            return true;
        }
        if (IcysBetterHorsesClient.MANAGE_KEY != null && IcysBetterHorsesClient.MANAGE_KEY.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void removed() {
        super.removed();
        ClientHorseRoster.clearFlash();
    }
}
