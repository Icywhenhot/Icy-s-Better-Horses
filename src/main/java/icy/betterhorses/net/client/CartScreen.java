package icy.betterhorses.net.client;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.inventory.CartMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Minecraft 1.21.1 cart management screen.
 */
public class CartScreen extends AbstractContainerScreen<CartMenu> {

    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_HIGHLIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int PANEL_OUTLINE = 0xFF000000;
    private static final int INK = 0xFF404040;
    private static final int MUTED = 0xFF707070;

    private final List<Button> tabButtons = new ArrayList<>();
    private final List<Button> pageButtons = new ArrayList<>();

    public CartScreen(CartMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = CartMenu.HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
        this.resizeForMenu();
    }

    private void resizeForMenu() {
        this.imageWidth = CartMenu.width(this.menu.type());
        this.inventoryLabelX = (this.imageWidth - 162) / 2 + 1;
    }

    @Override
    protected void init() {
        super.init();
        this.rebuildCartControls();
    }

    private void rebuildCartControls() {
        this.tabButtons.clear();
        this.pageButtons.clear();

        int x = this.leftPos;
        int y = this.topPos;
        int tabWidth = Math.max(48, (this.imageWidth - 14) / 3);
        for (int tab = 0; tab < 3; tab++) {
            final int selected = tab;
            Component label = Component.translatable("gui.icys-better-horses.cart.tab." + switch (tab) {
                case CartMenu.TAB_CARGO -> "cargo";
                case CartMenu.TAB_CART -> "cart";
                default -> "riders";
            });
            Button button = Button.builder(label, b -> this.selectTab(selected))
                    .bounds(x + 7 + tab * tabWidth, y - 20, tabWidth - 2, 20).build();
            button.active = tab != CartMenu.TAB_CARGO || this.hasChest();
            this.tabButtons.add(this.addRenderableWidget(button));
        }

        if (this.menu.tab() == CartMenu.TAB_CART) {
            this.addCartControls(x, y);
        } else if (this.menu.tab() == CartMenu.TAB_RIDERS) {
            this.addRiderControls(x, y);
        }
        this.refreshVisibility();
    }

    private void addCartControls(int x, int y) {
        CartType current = this.menu.type();
        CartType next = CartType.values()[(current.ordinal() + 1) % CartType.values().length];
        Button type = Button.builder(Component.translatable("gui.icys-better-horses.cart.switch").append(" ").append(next.displayName()),
                        b -> this.press(next.ordinal()))
                .bounds(x + 8, y + 29, Math.min(150, this.imageWidth - 16), 20).build();
        HorseCartEntity cart = this.menu.cart();
        if (cart != null) {
            type.active = cart.switchRefusal(next, this.menu.chest()) == null;
        }
        this.pageButtons.add(this.addRenderableWidget(type));

        int py = y + 54;
        List<CartType.Part> parts = current.parts();
        for (int i = 0; i < parts.size(); i++) {
            final int part = i;
            Component label = parts.get(i).label();
            Button button = Button.builder(label, b -> this.press(CartMenu.BUTTON_PART + part))
                    .bounds(x + 8, py, Math.min(150, this.imageWidth - 16), 18).build();
            this.pageButtons.add(this.addRenderableWidget(button));
            py += 20;
        }
    }

    private void addRiderControls(int x, int y) {
        int py = y + 28;
        Button pickup = Button.builder(Component.translatable("gui.icys-better-horses.cart.pickup"),
                        b -> this.press(CartMenu.BUTTON_PICKUP))
                .bounds(x + 8, py, Math.min(150, this.imageWidth - 16), 18).build();
        pickup.active = BhConfig.cartPickupEnabled();
        this.pageButtons.add(this.addRenderableWidget(pickup));
        py += 22;

        String[] filters = {"passive", "neutral", "hostile"};
        for (int i = 0; i < filters.length; i++) {
            final int id = CartMenu.BUTTON_PICKUP + 1 + i;
            Button filter = Button.builder(Component.translatable("gui.icys-better-horses.cart.filter." + filters[i]),
                            b -> this.press(id))
                    .bounds(x + 8, py, Math.min(150, this.imageWidth - 16), 18).build();
            filter.active = BhConfig.cartPickupEnabled();
            this.pageButtons.add(this.addRenderableWidget(filter));
            py += 20;
        }

        Button unload = Button.builder(Component.translatable("gui.icys-better-horses.cart.unload"),
                        b -> this.press(CartMenu.BUTTON_UNLOAD))
                .bounds(x + 8, py + 2, Math.min(150, this.imageWidth - 16), 18).build();
        unload.active = this.menu.cart() != null && !this.menu.cart().isPlaced() && this.menu.cart().cargoCount() > 0;
        this.pageButtons.add(this.addRenderableWidget(unload));
    }

    private boolean hasChest() {
        return this.menu.cart() != null && this.menu.cart().hasChest();
    }

    private void selectTab(int tab) {
        if (tab == CartMenu.TAB_CARGO && !this.hasChest()) return;
        this.menu.setTab(tab);
        this.press(CartMenu.BUTTON_TAB + tab);
        this.rebuildWidgets();
    }

    private void press(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    private void refreshVisibility() {
        for (int i = 0; i < this.tabButtons.size(); i++) {
            this.tabButtons.get(i).active = i != CartMenu.TAB_CARGO || this.hasChest();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.menu.tab() == CartMenu.TAB_CARGO && !this.hasChest()) {
            this.menu.setTab(CartMenu.TAB_CART);
            this.press(CartMenu.BUTTON_TAB + CartMenu.TAB_CART);
            this.rebuildWidgets();
            return;
        }
        if (this.menu.needsLayout()) {
            this.menu.layout();
            this.resizeForMenu();
            this.rebuildWidgets();
        }
        this.refreshVisibility();
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int right = x + this.imageWidth;
        int bottom = y + this.imageHeight;
        gfx.fill(x, y, right, bottom, PANEL_FILL);
        gfx.fill(x + 1, y + 1, right - 1, y + 3, PANEL_HIGHLIGHT);
        gfx.fill(x + 1, y + 1, x + 3, bottom - 1, PANEL_HIGHLIGHT);
        gfx.fill(x + 1, bottom - 3, right - 1, bottom - 1, PANEL_SHADOW);
        gfx.fill(right - 3, y + 1, right - 1, bottom - 1, PANEL_SHADOW);
        gfx.fill(x, y, right, y + 1, PANEL_OUTLINE);
        gfx.fill(x, bottom - 1, right, bottom, PANEL_OUTLINE);
        gfx.fill(x, y, x + 1, bottom, PANEL_OUTLINE);
        gfx.fill(right - 1, y, right, bottom, PANEL_OUTLINE);

        for (Slot slot : this.menu.slots) {
            if (slot.isActive()) {
                gfx.blitSprite(SLOT_SPRITE, x + slot.x - 1, y + slot.y - 1, 18, 18);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        Component name = this.menu.cart() != null && this.menu.cart().hasCustomName()
                ? this.menu.cart().getCustomName() : this.menu.type().displayName();
        gfx.drawString(this.font, name, this.titleLabelX, this.titleLabelY, INK, false);
        gfx.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, INK, false);
        if (this.menu.tab() == CartMenu.TAB_RIDERS && !BhConfig.cartPickupEnabled()) {
            Component off = Component.translatable("gui.icys-better-horses.cart.pickup.server_off");
            gfx.drawString(this.font, off, 8, 116, MUTED, false);
        }
    }
}
