package icy.betterhorses.net.client;

import icy.betterhorses.net.inventory.CartChestMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CartChestScreen extends AbstractContainerScreen<CartChestMenu> {

    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");

    private static final int BORDER = 7;
    private static final int WIDTH = BORDER * 2 + CartChestMenu.COLUMNS * 18;
    private static final int HEIGHT = 222;

    private static final int PANEL_FILL = 0xFFC6C6C6;
    private static final int PANEL_HIGHLIGHT = 0xFFFFFFFF;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int PANEL_OUTLINE = 0xFF000000;

    private static final int CHEST_TOP = 17;
    private static final int PLAYER_LEFT = 61;
    private static final int PLAYER_TOP = 139;
    private static final int HOTBAR_TOP = 197;

    public CartChestScreen(CartChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
        this.inventoryLabelX = PLAYER_LEFT + 1;
        this.inventoryLabelY = HEIGHT - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gfx, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;
        int right = x + WIDTH;
        int bottom = y + HEIGHT;

        gfx.fill(x, y, right, bottom, PANEL_FILL);
        gfx.fill(x + 1, y + 1, right - 1, y + 3, PANEL_HIGHLIGHT);
        gfx.fill(x + 1, y + 1, x + 3, bottom - 1, PANEL_HIGHLIGHT);
        gfx.fill(x + 1, bottom - 3, right - 1, bottom - 1, PANEL_SHADOW);
        gfx.fill(right - 3, y + 1, right - 1, bottom - 1, PANEL_SHADOW);
        gfx.fill(x, y, right, y + 1, PANEL_OUTLINE);
        gfx.fill(x, bottom - 1, right, bottom, PANEL_OUTLINE);
        gfx.fill(x, y, x + 1, bottom, PANEL_OUTLINE);
        gfx.fill(right - 1, y, right, bottom, PANEL_OUTLINE);

        for (int row = 0; row < CartChestMenu.ROWS; row++) {
            for (int col = 0; col < CartChestMenu.COLUMNS; col++) {
                slot(gfx, x + BORDER + col * 18, y + CHEST_TOP + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slot(gfx, x + PLAYER_LEFT + col * 18, y + PLAYER_TOP + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slot(gfx, x + PLAYER_LEFT + col * 18, y + HOTBAR_TOP);
        }
    }

    private static void slot(GuiGraphicsExtractor gfx, int x, int y) {
        gfx.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, x, y, 18, 18);
    }
}
