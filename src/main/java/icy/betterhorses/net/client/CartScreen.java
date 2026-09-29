package icy.betterhorses.net.client;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.inventory.CartMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;

public class CartScreen extends AbstractContainerScreen<CartMenu> {

    private static final Identifier HORSE_ICON =
            Identifier.fromNamespaceAndPath("icys-better-horses", "textures/gui/switcher/horse.png");
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private static final Identifier BUTTON = Identifier.withDefaultNamespace("widget/button");
    private static final Identifier BUTTON_OFF = Identifier.withDefaultNamespace("widget/button_disabled");
    private static final Identifier BUTTON_LIT = Identifier.withDefaultNamespace("widget/button_highlighted");

    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_HIGHLIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int PANEL_OUTLINE = 0xFF000000;
    private static final int INK = 0xFF404040;
    private static final int MUTED = 0xFF707070;
    private static final int FADED = 0xFF969696;
    private static final int ENABLED = 0xFF2E6E24;
    private static final int DISABLED = 0xFF8C8C8C;
    private static final int SERVER_OFF = 0xFF962828;
    private static final int GOLD = 0xFFFFD25A;

    private static final int TAB_W = 26;
    private static final int TAB_H = 28;
    private static final int TAB_STEP = 27;

    private static final int WELL_X = 7;
    private static final int WELL_Y = 17;
    private static final int WELL_H = 64;
    private static final int ACTION_Y = 107;
    private static final float PREVIEW_YAW = -125.0F;
    private static final float PREVIEW_PITCH = 22.0F;
    private static final float DEG = (float) Math.PI / 180.0F;

    private static final String[] FILTERS = {"passive", "neutral", "hostile"};

    private final Inventory inventory;
    private final List<AbstractButton> cartWidgets = new ArrayList<>();
    private final List<AbstractButton> riderWidgets = new ArrayList<>();
    private Button switchButton;
    private Button unloadButton;
    private int view;
    private @Nullable HorseCartEntity preview;
    private @Nullable CartType previewType;
    private boolean errored;

    public CartScreen(CartMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CartMenu.width(menu.type()), CartMenu.HEIGHT);
        this.inventory = inventory;
        this.inventoryLabelY = this.imageHeight - 94;
        this.inventoryLabelX = (this.imageWidth - 162) / 2 + 1;
        this.view = menu.type().ordinal();
    }

    private @Nullable HorseCartEntity cart() {
        return this.menu.cart();
    }

    private CartType viewed() {
        return CartType.byOrdinal(this.view);
    }

    private boolean browsing() {
        return this.viewed() != this.menu.type();
    }

    private int wellWidth() {
        return this.imageWidth - WELL_X * 2;
    }

    @Override
    protected void init() {
        super.init();
        this.topPos = (this.height - this.imageHeight + TAB_H) / 2;
        this.cartWidgets.clear();
        this.riderWidgets.clear();
        int x = this.leftPos;
        int y = this.topPos;
        int mid = x + this.imageWidth / 2;

        this.cartWidgets.add(this.addRenderableWidget(Button.builder(Component.literal("<"), b -> this.step(-1))
                .bounds(x + WELL_X + 3, y + WELL_Y + 20, 14, 14).build()));
        this.cartWidgets.add(this.addRenderableWidget(Button.builder(Component.literal(">"), b -> this.step(1))
                .bounds(x + WELL_X + this.wellWidth() - 17, y + WELL_Y + 20, 14, 14).build()));
        this.switchButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.icys-better-horses.cart.switch"),
                        b -> this.press(this.view))
                .bounds(mid - 60, y + ACTION_Y, 120, 16).build());
        this.cartWidgets.add(this.switchButton);

        List<CartType.Part> parts = this.menu.type().parts();
        int right = x + WELL_X + this.wellWidth() - 3;
        for (int i = parts.size() - 1; i >= 0; i--) {
            int index = i;
            Component label = parts.get(i).label();
            int w = this.font.width(label) + 16;
            right -= w;
            this.cartWidgets.add(this.addRenderableWidget(new Chip(right, y + WELL_Y + 3, w, label,
                    () -> this.cart() != null && !this.cart().partHidden(index),
                    () -> this.press(CartMenu.BUTTON_PART + index))));
            right -= 3;
        }

        this.riderWidgets.add(this.addRenderableWidget(new Switch(x + this.imageWidth - 29, y + 18)));
        for (int i = 0; i < FILTERS.length; i++) {
            int flag = HorseCartEntity.PICKUP_PASSIVE << i;
            int id = CartMenu.BUTTON_PICKUP + 1 + i;
            Component label = Component.translatable("gui.icys-better-horses.cart.filter." + FILTERS[i]);
            Tick tick = new Tick(x + 8, y + 45 + i * 14, label, () -> (this.pickup() & flag) != 0, () -> this.press(id));
            tick.setTooltip(Tooltip.create(Component.translatable(
                    "gui.icys-better-horses.cart.filter." + FILTERS[i] + ".hint")));
            this.riderWidgets.add(this.addRenderableWidget(tick));
        }
        this.unloadButton = this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.icys-better-horses.cart.unload"),
                        b -> this.press(CartMenu.BUTTON_UNLOAD))
                .bounds(mid - 40, y + 110, 80, 18).build());
        this.riderWidgets.add(this.unloadButton);

        if (this.ridingBoundHorse()) {
            this.addRenderableWidget(new BackToHorse(x - 18, y + 24));
        }
        this.refresh();
    }

    private boolean ridingBoundHorse() {
        HorseCartEntity cart = this.cart();
        return cart != null && cart.boundHorse() != null
                && this.minecraft.player.getVehicle() == cart.boundHorse();
    }

    private void step(int dir) {
        this.view = Math.floorMod(this.view + dir, CartType.values().length);
        this.refresh();
    }

    private void press(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    private int pickup() {
        HorseCartEntity cart = this.cart();
        return cart == null ? 0 : cart.pickup();
    }

    private boolean pickupOn() {
        return BhConfig.cartPickupEnabled() && (this.pickup() & HorseCartEntity.PICKUP_ON) != 0;
    }

    private @Nullable HorseCartEntity.Refusal refusal() {
        HorseCartEntity cart = this.cart();
        return cart == null ? null : cart.switchRefusal(this.viewed(), this.menu.chest());
    }

    private int cargo() {
        HorseCartEntity cart = this.cart();
        return cart == null ? 0 : cart.cargoCount();
    }

    private boolean hasChest() {
        return this.cart() != null && this.cart().hasChest();
    }

    private void selectTab(int tab) {
        if (tab == this.menu.tab()) {
            return;
        }
        this.menu.setTab(tab);
        this.press(CartMenu.BUTTON_TAB + tab);
        this.refresh();
    }

    private void refresh() {
        int tab = this.menu.tab();
        for (AbstractButton widget : this.cartWidgets) {
            widget.visible = tab == CartMenu.TAB_CART;
            if (widget instanceof Chip) {
                widget.visible &= !this.browsing();
            }
        }
        for (AbstractButton widget : this.riderWidgets) {
            widget.visible = tab == CartMenu.TAB_RIDERS;
        }
        this.switchButton.visible &= this.browsing();
        HorseCartEntity.Refusal refusal = this.refusal();
        this.switchButton.active = refusal == null;
        this.switchButton.setMessage(refusal == null
                ? Component.translatable("gui.icys-better-horses.cart.switch")
                : refusal.label());
        HorseCartEntity cart = this.cart();
        this.unloadButton.active = cart != null && !cart.isPlaced() && this.cargo() > 0;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.menu.tab() == CartMenu.TAB_CARGO && !this.hasChest()) {
            this.selectTab(CartMenu.TAB_CART);
        }
        if (this.menu.needsLayout()) {
            this.menu.layout();
            if (CartMenu.width(this.menu.type()) != this.imageWidth) {
                RiderPanel.rememberCursor();
                this.minecraft.gui.setScreen(new CartScreen(this.menu, this.inventory, this.title));
                return;
            }
            this.view = this.menu.type().ordinal();
            this.rebuildWidgets();
        }
        this.refresh();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gfx, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        int tab = this.menu.tab();

        for (int i = 0; i < 3; i++) {
            if (i != tab) {
                this.drawTab(gfx, i, false);
            }
        }
        this.drawPanel(gfx, x, y, this.imageWidth, this.imageHeight);
        this.drawTab(gfx, tab, true);

        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
                gfx.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, x + slot.x - 1, y + slot.y - 1, 18, 18);
            }
        }

        if (tab == CartMenu.TAB_CART) {
            this.drawWell(gfx, x + WELL_X, y + WELL_Y, this.wellWidth(), WELL_H);
        } else if (tab == CartMenu.TAB_RIDERS) {
            gfx.fill(x + 7, y + 87, x + this.imageWidth - 7, y + 88, 0xFF8B8B8B);
            gfx.fill(x + 7, y + 88, x + this.imageWidth - 7, y + 89, PANEL_HIGHLIGHT);
        }
    }

    private void drawPanel(GuiGraphicsExtractor gfx, int x, int y, int width, int height) {
        int right = x + width;
        int bottom = y + height;
        gfx.fill(x, y, right, bottom, PANEL_FILL);
        gfx.fill(x + 1, y + 1, right - 1, y + 3, PANEL_HIGHLIGHT);
        gfx.fill(x + 1, y + 1, x + 3, bottom - 1, PANEL_HIGHLIGHT);
        gfx.fill(x + 1, bottom - 3, right - 1, bottom - 1, PANEL_SHADOW);
        gfx.fill(right - 3, y + 1, right - 1, bottom - 1, PANEL_SHADOW);
        gfx.fill(x, y, right, y + 1, PANEL_OUTLINE);
        gfx.fill(x, bottom - 1, right, bottom, PANEL_OUTLINE);
        gfx.fill(x, y, x + 1, bottom, PANEL_OUTLINE);
        gfx.fill(right - 1, y, right, bottom, PANEL_OUTLINE);
    }

    private void drawTab(GuiGraphicsExtractor gfx, int tab, boolean selected) {
        int x = this.leftPos + tab * TAB_STEP;
        int y = this.topPos - TAB_H;
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/creative_inventory/tab_top_"
                + (selected ? "selected_" : "unselected_") + (tab + 1)), x, y, TAB_W, 32);
        ItemStack icon = switch (tab) {
            case CartMenu.TAB_CARGO -> new ItemStack(Items.CHEST);
            case CartMenu.TAB_CART -> new ItemStack(ModItems.HORSE_CART);
            default -> new ItemStack(Items.SADDLE);
        };
        gfx.item(icon, x + 5, y + 9 + (selected ? 0 : 1));
        if (tab == CartMenu.TAB_CARGO && !this.hasChest()) {
            gfx.nextStratum();
            gfx.fill(x + 4, y + 8, x + 22, y + 27, 0xAA8B8B8B);
        }
    }

    private void drawWell(GuiGraphicsExtractor gfx, int x, int y, int w, int h) {
        gfx.fill(x, y, x + w, y + h, 0xFF000000);
        gfx.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        gfx.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        gfx.fill(x + 1, y + h - 1, x + w, y + h, PANEL_HIGHLIGHT);
        gfx.fill(x + w - 1, y + 1, x + w, y + h, PANEL_HIGHLIGHT);

        CartType type = this.viewed();
        HorseCartEntity model = this.previewFor(type);
        if (model != null) {
            this.drawCart(gfx, model, type, x + 1, y + 1, x + w - 1, y + h - 13);
        }

        gfx.nextStratum();
        if (this.lockedView()) {
            gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x99000000);
            this.drawLock(gfx, x + w / 2 - 7, y + 12);
            Component note = Component.translatable("gui.icys-better-horses.cart.refuse.draft");
            gfx.text(this.font, note, x + (w - this.font.width(note)) / 2, y + 34, GOLD, true);
        }
        gfx.fill(x + 1, y + h - 13, x + w - 1, y + h - 1, 0xAA000000);
        gfx.text(this.font, type.displayName(), x + 4, y + h - 11, 0xFFFFFFFF, true);
        Component stats = Component.translatable("gui.icys-better-horses.cart.stats", type.seats(), type.chestSlots());
        gfx.text(this.font, stats, x + w - 4 - this.font.width(stats), y + h - 11, 0xFFAAAAAA, true);
    }

    private boolean lockedView() {
        return this.refusal() == HorseCartEntity.Refusal.DRAFT_ONLY;
    }

    private void drawLock(GuiGraphicsExtractor gfx, int x, int y) {
        int dark = 0xFF303030;
        int gold = 0xFFE0B73A;
        gfx.fill(x + 4, y, x + 10, y + 2, dark);
        gfx.fill(x + 2, y + 2, x + 4, y + 6, dark);
        gfx.fill(x + 10, y + 2, x + 12, y + 6, dark);
        gfx.fill(x, y + 6, x + 14, y + 18, dark);
        gfx.fill(x + 2, y + 8, x + 12, y + 16, gold);
        gfx.fill(x + 6, y + 10, x + 8, y + 14, dark);
    }

    private @Nullable HorseCartEntity previewFor(CartType type) {
        HorseCartEntity cart = this.cart();
        if (this.minecraft.level == null || this.errored) {
            return null;
        }
        if (this.preview == null || this.previewType != type) {
            this.preview = HorseCartEntity.preview(this.minecraft.level, type, cart != null && cart.isPlaced());
            this.previewType = type;
        }
        boolean mine = cart != null && !this.browsing();
        int hidden = 0;
        if (mine) {
            for (int i = 0; i < type.parts().size(); i++) {
                if (cart.partHidden(i)) {
                    hidden |= 1 << i;
                }
            }
        }
        this.preview.dressPreview(mine && cart.hasChest(), mine && cart.hasPlough(), hidden);
        return this.preview;
    }

    private void drawCart(GuiGraphicsExtractor gfx, HorseCartEntity model, CartType type, int x0, int y0, int x1, int y1) {
        model.setYRot(PREVIEW_YAW);
        model.setYBodyRot(PREVIEW_YAW);
        model.setYHeadRot(PREVIEW_YAW);
        Quaternionf pitch = new Quaternionf().rotateX(-PREVIEW_PITCH * DEG);
        Quaternionf flip = new Quaternionf().rotateZ((float) Math.PI).mul(pitch);
        try {
            EntityRenderer renderer = this.minecraft.getEntityRenderDispatcher().getRenderer(model);
            EntityRenderState state = renderer.createRenderState(model, 1.0F);
            state.shadowPieces.clear();
            state.outlineColor = 0;
            Vec3 shift = new Vec3(0.0D, 0.0D, type.bedCenterBehind()).yRot(-PREVIEW_YAW * DEG).scale(-1.0D);
            Vector3f offset = new Vector3f((float) shift.x, (type.isLarge() ? 3.1F : 1.7F) / 2.0F, (float) shift.z);
            gfx.entity(state, type.isLarge() ? 16.0F : 23.0F, offset, flip, pitch, x0, y0, x1, y1);
        } catch (Exception exception) {
            this.errored = true;
            IcysBetterHorses.LOGGER.warn("[cart] could not draw the cart preview", exception);
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractContents(gfx, mouseX, mouseY, partialTick);
        gfx.nextStratum();
        if (this.menu.tab() == CartMenu.TAB_CART && this.browsing()) {
            for (int i = CartMenu.RIG_START; i < CartMenu.RIG_START + CartType.Attachment.values().length; i++) {
                Slot slot = this.menu.slots.get(i);
                if (slot.isActive()) {
                    int sx = this.leftPos + slot.x;
                    int sy = this.topPos + slot.y;
                    gfx.fill(sx, sy, sx + 16, sy + 16, 0x99C6C6C6);
                }
            }
        }
        float intensity = BhSlotFlash.intensity();
        int flashed = BhSlotFlash.flashingSlot();
        if (intensity > 0.0F && flashed >= 0 && flashed < this.menu.slots.size()) {
            Slot slot = this.menu.slots.get(flashed);
            int sx = this.leftPos + slot.x;
            int sy = this.topPos + slot.y;
            gfx.fill(sx, sy, sx + 16, sy + 16, BhAnim.fade(BhScreenDraw.BTN_ERROR, intensity * 0.65F));
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        HorseCartEntity cart = this.cart();
        Component name = cart != null && cart.hasCustomName() ? cart.getCustomName() : this.menu.type().displayName();
        gfx.text(this.font, name, this.titleLabelX, this.titleLabelY, INK, false);
        gfx.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, INK, false);

        CartType badgeType = this.menu.tab() == CartMenu.TAB_CART ? this.viewed() : this.menu.type();
        Component badge = Component.translatable(badgeType.isLarge()
                ? "gui.icys-better-horses.cart.large" : "gui.icys-better-horses.cart.small");
        boolean locked = this.menu.tab() == CartMenu.TAB_CART && this.lockedView();
        int bw = this.font.width(badge) + 6 + (locked ? 9 : 0);
        int bx = this.imageWidth - 7 - bw;
        gfx.fill(bx, 4, bx + bw, 15, 0xFF787878);
        gfx.fill(bx, 4, bx + bw - 1, 5, 0xFF373737);
        gfx.fill(bx, 4, bx + 1, 14, 0xFF373737);
        gfx.fill(bx + 1, 14, bx + bw, 15, PANEL_HIGHLIGHT);
        gfx.fill(bx + bw - 1, 5, bx + bw, 15, PANEL_HIGHLIGHT);
        gfx.text(this.font, badge, bx + 3, 6, 0xFFFFFFFF, true);
        if (locked) {
            gfx.pose().pushMatrix();
            gfx.pose().translate(bx + bw - 9, 5);
            gfx.pose().scale(0.5F, 0.5F);
            this.drawLock(gfx, 0, 0);
            gfx.pose().popMatrix();
        }

        int tab = this.menu.tab();
        if (tab == CartMenu.TAB_CART && !this.browsing()) {
            Component current = Component.translatable("gui.icys-better-horses.cart.current");
            gfx.text(this.font, current, (this.imageWidth - this.font.width(current)) / 2, ACTION_Y + 4, MUTED, false);
        } else if (tab == CartMenu.TAB_RIDERS) {
            boolean serverOn = BhConfig.cartPickupEnabled();
            gfx.text(this.font, Component.translatable("gui.icys-better-horses.cart.pickup"),
                    8, 20, serverOn ? INK : FADED, false);
            Component status;
            int color;
            if (!serverOn) {
                status = Component.translatable("gui.icys-better-horses.cart.pickup.server_off");
                color = SERVER_OFF;
            } else if (this.pickupOn()) {
                status = Component.translatable("gui.icys-better-horses.cart.pickup.on");
                color = ENABLED;
            } else {
                status = Component.translatable("gui.icys-better-horses.cart.pickup.off");
                color = DISABLED;
            }
            gfx.text(this.font, status, 8, 31, color, false);

            Component riders;
            if (cart != null && cart.isPlaced()) {
                riders = Component.translatable("gui.icys-better-horses.cart.riders.parked");
            } else {
                int n = this.cargo();
                riders = n == 0 ? Component.translatable("gui.icys-better-horses.cart.riders.none")
                        : Component.translatable(n == 1 ? "gui.icys-better-horses.cart.riders.one"
                        : "gui.icys-better-horses.cart.riders.many", n);
            }
            gfx.text(this.font, riders, (this.imageWidth - this.font.width(riders)) / 2, 96, MUTED, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        super.extractTooltip(gfx, mouseX, mouseY);
        int tab = this.hoveredTab(mouseX, mouseY);
        if (tab >= 0) {
            Component label = Component.translatable("gui.icys-better-horses.cart.tab." + switch (tab) {
                case CartMenu.TAB_CARGO -> this.hasChest() ? "cargo" : "cargo.locked";
                case CartMenu.TAB_CART -> "cart";
                default -> "riders";
            });
            gfx.setTooltipForNextFrame(this.font, label, mouseX, mouseY);
            return;
        }
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot instanceof CartMenu.RigSlot rig && !rig.hasItem()) {
            gfx.setTooltipForNextFrame(this.font, Component.translatable(
                    "gui.icys-better-horses.cart.slot." + rig.kind().name().toLowerCase(Locale.ROOT)),
                    mouseX, mouseY);
        }
    }

    private int hoveredTab(double mouseX, double mouseY) {
        for (int i = 0; i < 3; i++) {
            int x = this.leftPos + i * TAB_STEP;
            int y = this.topPos - TAB_H;
            if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < this.topPos) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int tab = this.hoveredTab(event.x(), event.y());
        if (tab >= 0 && event.button() == 0) {
            if (tab != CartMenu.TAB_CARGO || this.hasChest()) {
                this.selectTab(tab);
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        return super.hasClickedOutside(mouseX, mouseY, left, top) && this.hoveredTab(mouseX, mouseY) < 0;
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ContainerInput input) {
        if (slot instanceof CartMenu.RigSlot rig && rig.kind() == CartType.Attachment.CHEST
                && rig.hasItem() && !this.menu.chestEmpty()) {
            BhSlotFlash.trigger(slotId);
            this.minecraft.player.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.cart_chest_in_use"));
            return;
        }
        super.slotClicked(slot, slotId, mouseButton, input);
    }

    private static void buttonSprite(GuiGraphicsExtractor gfx, boolean on, boolean lit, int x, int y, int w, int h) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, !on ? BUTTON_OFF : lit ? BUTTON_LIT : BUTTON, x, y, w, h);
    }

    private final class Chip extends AbstractButton {

        private final BooleanSupplier on;
        private final Runnable action;

        Chip(int x, int y, int width, Component label, BooleanSupplier on, Runnable action) {
            super(x, y, width, 13, label);
            this.on = on;
            this.action = action;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            this.action.run();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
            boolean lit = this.on.getAsBoolean();
            buttonSprite(gfx, lit, this.isHoveredOrFocused(), this.getX(), this.getY(), this.width, this.height);
            int bx = this.getX() + 3;
            int by = this.getY() + 3;
            gfx.fill(bx, by, bx + 7, by + 7, 0xFF373737);
            gfx.fill(bx + 1, by + 1, bx + 7, by + 7, PANEL_HIGHLIGHT);
            gfx.fill(bx + 1, by + 1, bx + 6, by + 6, lit ? 0xFF5CAA3C : 0xFF464646);
            gfx.text(font, this.getMessage(), this.getX() + 13, this.getY() + 3,
                    lit ? 0xFFFFFFFF : 0xFFA0A0A0, true);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private final class Switch extends AbstractButton {

        Switch(int x, int y) {
            super(x, y, 22, 12, Component.translatable("gui.icys-better-horses.cart.pickup"));
            if (!BhConfig.cartPickupEnabled()) {
                this.setTooltip(Tooltip.create(Component.translatable("gui.icys-better-horses.cart.pickup.server_off")));
            }
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (BhConfig.cartPickupEnabled()) {
                press(CartMenu.BUTTON_PICKUP);
            }
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
            boolean serverOn = BhConfig.cartPickupEnabled();
            boolean on = pickupOn();
            int x = this.getX();
            int y = this.getY();
            gfx.fill(x, y, x + 22, y + 12, on ? 0xFF5CAA3C : 0xFF5F5F5F);
            gfx.fill(x, y, x + 21, y + 1, 0xFF373737);
            gfx.fill(x, y, x + 1, y + 11, 0xFF373737);
            gfx.fill(x + 1, y + 11, x + 22, y + 12, PANEL_HIGHLIGHT);
            gfx.fill(x + 21, y + 1, x + 22, y + 12, PANEL_HIGHLIGHT);
            buttonSprite(gfx, serverOn, this.isHoveredOrFocused(), on ? x + 11 : x + 1, y + 1, 10, 10);
            if (!serverOn) {
                gfx.pose().pushMatrix();
                gfx.pose().translate(x - 9, y + 1);
                gfx.pose().scale(0.5F, 0.5F);
                drawLock(gfx, 0, 0);
                gfx.pose().popMatrix();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private final class Tick extends AbstractButton {

        private final BooleanSupplier on;
        private final Runnable action;

        Tick(int x, int y, Component label, BooleanSupplier on, Runnable action) {
            super(x, y, 14 + Minecraft.getInstance().font.width(label), 10, label);
            this.on = on;
            this.action = action;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            if (pickupOn()) {
                this.action.run();
            }
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
            boolean live = pickupOn();
            int x = this.getX();
            int y = this.getY();
            gfx.fill(x, y, x + 10, y + 10, live ? 0xFF8B8B8B : 0xFF787878);
            gfx.fill(x, y, x + 9, y + 1, 0xFF373737);
            gfx.fill(x, y, x + 1, y + 9, 0xFF373737);
            gfx.fill(x + 1, y + 9, x + 10, y + 10, PANEL_HIGHLIGHT);
            gfx.fill(x + 9, y + 1, x + 10, y + 10, PANEL_HIGHLIGHT);
            if (this.on.getAsBoolean()) {
                int mark = live ? 0xFFFFFFFF : 0xFF969696;
                int[][] pts = {{2, 5}, {3, 6}, {4, 7}, {5, 6}, {6, 5}, {7, 4}, {8, 3}};
                for (int[] p : pts) {
                    gfx.fill(x + p[0], y + p[1] - 1, x + p[0] + 1, y + p[1] + 1, mark);
                }
            }
            if (live && this.isHoveredOrFocused()) {
                gfx.fill(x + 1, y + 1, x + 9, y + 9, 0x30FFFFFF);
            }
            gfx.text(font, this.getMessage(), x + 14, y + 1, live ? INK : FADED, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }

    private final class BackToHorse extends AbstractButton {

        BackToHorse(int x, int y) {
            super(x, y, 16, 21, Component.translatable("gui.icys-better-horses.cart.to_horse"));
            this.setTooltip(Tooltip.create(this.getMessage()));
        }

        @Override
        public void onPress(InputWithModifiers input) {
            RiderPanel.rememberCursor();
            minecraft.player.sendOpenInventory();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
            gfx.blit(RenderPipelines.GUI_TEXTURED, HORSE_ICON, this.getX(), this.getY(), 0.0F, 0.0F, 16, 21, 16, 21);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            this.defaultButtonNarrationText(output);
        }
    }
}
