package icy.betterhorses.net.inventory;

import icy.betterhorses.net.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CartChestMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 15;
    public static final int ROWS = 6;
    public static final int SLOTS = COLUMNS * ROWS;

    private static final int PANEL_LEFT = 8;
    private static final int PANEL_TOP = 18;
    private static final int PLAYER_LEFT = 62;
    private static final int PLAYER_TOP = 140;
    private static final int HOTBAR_TOP = 198;

    private final Container container;

    public CartChestMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOTS));
    }

    public CartChestMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenus.CART_CHEST, containerId);
        checkContainerSize(container, SLOTS);
        this.container = container;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                this.addSlot(new Slot(container, col + row * COLUMNS,
                        PANEL_LEFT + col * 18, PANEL_TOP + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_LEFT + col * 18, PLAYER_TOP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_LEFT + col * 18, HOTBAR_TOP));
        }
    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < SLOTS) {
            if (!this.moveItemStackTo(stack, SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
