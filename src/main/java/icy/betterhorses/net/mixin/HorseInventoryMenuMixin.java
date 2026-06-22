package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin extends AbstractContainerMenu implements HorseInventoryLayoutAccess {

    @Shadow @Final
    private Container horseContainer;

    @Shadow @Final
    private AbstractHorse horse;

    @Unique private static final int BH_GEAR_SLOT_X = 80;
    @Unique private static final int BH_GEAR_SLOT_Y = 18;
    @Unique private static final int BH_CHEST_SLOT_X = 8;
    @Unique private static final int BH_CHEST_SLOT_Y = 79;
    @Unique private static final int BH_CHEST_SLOT_COUNT = 27;
    @Unique private static final int BH_PLAYER_SLOT_Y_OFFSET = 54;

    @Unique private int bh_gearStartIndex = -1;
    @Unique private int bh_chestStartIndex = -1;
    @Unique private int bh_playerInventoryStartIndex = -1;
    @Unique private int bh_playerInventoryEndIndex = -1;
    @Unique private boolean bh_playerInventoryShifted = false;

    /** Snapshot view of the player's ender chest, shown in the horse's chest slots when an Ender
     *  Chest is in the chest gear slot. Synced back to the real ender chest on close / removal. */
    @Unique private final SimpleContainer bh_enderChestView = new SimpleContainer(BH_CHEST_SLOT_COUNT);
    @Unique private PlayerEnderChestContainer bh_playerEnderChest = null;
    @Unique private boolean bh_enderChestViewLoaded = false;
    @Unique private SimpleContainer bh_gearForListener = null;
    @Unique private SimpleContainer bh_chestForListener = null;

    protected HorseInventoryMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_appendUpgradedSaddleSlots(
            int containerId,
            Inventory playerInventory,
            Container horseContainer,
            AbstractHorse horse,
            CallbackInfo ci) {
        final IHorseData data = (IHorseData) horse;
        final SimpleContainer gear = data.bh_getGearContainer();
        final SimpleContainer chest = data.bh_getChestContainer();
        this.bh_gearForListener = gear;
        this.bh_chestForListener = chest;
        // Server side: snapshot the player's ender chest so the view container can mirror it.
        this.bh_playerEnderChest = playerInventory.player.level().isClientSide()
                ? null
                : playerInventory.player.getEnderChestInventory();
        if (this.bh_isEnderChestGear(gear.getItem(GearSlot.CHEST.ordinal()))) {
            this.bh_loadEnderChestView();
        }

        // Delegate container that swaps storage based on which chest item is in the gear slot.
        final Container extraStorage = new Container() {
            private Container bh_active() {
                return HorseInventoryMenuMixin.this.bh_isEnderChestGear(gear.getItem(GearSlot.CHEST.ordinal()))
                        ? HorseInventoryMenuMixin.this.bh_enderChestView
                        : chest;
            }

            @Override public int getContainerSize() { return chest.getContainerSize(); }
            @Override public boolean isEmpty() { return this.bh_active().isEmpty(); }
            @Override public ItemStack getItem(int slot) { return this.bh_active().getItem(slot); }
            @Override public ItemStack removeItem(int slot, int amount) { return this.bh_active().removeItem(slot, amount); }
            @Override public ItemStack removeItemNoUpdate(int slot) { return this.bh_active().removeItemNoUpdate(slot); }
            @Override public void setItem(int slot, ItemStack stack) { this.bh_active().setItem(slot, stack); }
            @Override public void setChanged() { this.bh_active().setChanged(); }
            @Override public boolean stillValid(Player player) { return this.bh_active().stillValid(player); }
            @Override public void startOpen(Player user) { this.bh_active().startOpen(user); }
            @Override public void stopOpen(Player user) { this.bh_active().stopOpen(user); }
            @Override public void clearContent() { this.bh_active().clearContent(); }
        };

        // 1.20.1 HorseInventoryMenu adds saddle + armor + (chest) slots, then the 36 player-inventory
        // slots last. We inject at TAIL (before adding our own slots), so the player inventory is
        // exactly the final 36 vanilla slots — robust regardless of horse/chest slot count.
        this.bh_playerInventoryStartIndex = this.slots.size() - 36;
        this.bh_playerInventoryEndIndex = this.slots.size();

        this.bh_gearStartIndex = this.slots.size();
        for (GearSlot slot : GearSlot.values()) {
            final GearSlot type = slot;
            this.addSlot(new Slot(gear, slot.ordinal(), BH_GEAR_SLOT_X + slot.ordinal() * 18, BH_GEAR_SLOT_Y) {
                @Override public boolean mayPlace(ItemStack stack) { return type.accepts(stack); }
                @Override public boolean isActive() { return HorseInventoryMenuMixin.this.bh_hasUpgradedSaddleInMenu(); }
                @Override public int getMaxStackSize() { return 1; }

                @Override
                public void set(ItemStack stack) {
                    ItemStack previousStack = this.getItem().copy();
                    super.set(stack);
                    if (type == GearSlot.CHEST) {
                        HorseInventoryMenuMixin.this.bh_handleChestGearChange(previousStack, stack, data);
                    }
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    super.onTake(player, stack);
                    if (type == GearSlot.CHEST) {
                        data.bh_onChestGearRemoved(stack);
                        if (HorseInventoryMenuMixin.this.bh_isEnderChestGear(stack)) {
                            HorseInventoryMenuMixin.this.bh_saveEnderChestView();
                        }
                    }
                }
            });
        }

        this.bh_chestStartIndex = this.slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                final int index = col + row * 9;
                this.addSlot(new Slot(extraStorage, index, BH_CHEST_SLOT_X + col * 18, BH_CHEST_SLOT_Y + row * 18) {
                    @Override
                    public boolean isActive() {
                        return HorseInventoryMenuMixin.this.bh_hasUpgradedSaddleInMenu()
                                && HorseInventoryMenuMixin.this.bh_isChestGear(gear.getItem(GearSlot.CHEST.ordinal()));
                    }
                });
            }
        }

        this.bh_refreshLayout();
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void bh_quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size() || bh_gearStartIndex < 0 || bh_chestStartIndex < 0) {
            return;
        }

        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copiedStack = sourceStack.copy();

        int horseSlotEnd = this.bh_playerInventoryStartIndex;
        int playerInventoryStart = horseSlotEnd;
        int playerInventoryEnd = playerInventoryStart + 27;
        int hotbarStart = playerInventoryEnd;
        int hotbarEnd = hotbarStart + 9;

        boolean moved;
        if (index < horseSlotEnd || index >= bh_gearStartIndex) {
            moved = this.moveItemStackTo(sourceStack, playerInventoryStart, hotbarEnd, true);
        } else {
            moved = this.getSlot(1).mayPlace(sourceStack)
                    && !this.getSlot(1).hasItem()
                    && this.moveItemStackTo(sourceStack, 1, 2, false);

            if (!moved) {
                moved = this.getSlot(0).mayPlace(sourceStack)
                        && !this.getSlot(0).hasItem()
                        && this.moveItemStackTo(sourceStack, 0, 1, false);
            }

            if (!moved && this.bh_hasUpgradedSaddleInMenu()) {
                moved = bh_moveIntoFirstMatchingGearSlot(sourceStack);
                if (!moved && this.bh_hasChestGearInMenu()) {
                    moved = this.moveItemStackTo(sourceStack, bh_chestStartIndex, bh_chestStartIndex + BH_CHEST_SLOT_COUNT, false);
                }
            }

            if (!moved && horseSlotEnd > 2) {
                moved = this.moveItemStackTo(sourceStack, 2, horseSlotEnd, false);
            }

            if (!moved) {
                if (index < playerInventoryEnd) {
                    moved = this.moveItemStackTo(sourceStack, hotbarStart, hotbarEnd, false);
                } else if (index < hotbarEnd) {
                    moved = this.moveItemStackTo(sourceStack, playerInventoryStart, playerInventoryEnd, false);
                } else {
                    moved = this.moveItemStackTo(sourceStack, playerInventoryStart, hotbarEnd, false);
                }
            }
        }

        if (!moved) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (sourceStack.isEmpty()) {
            int chestGearSlotIndex = this.bh_gearStartIndex + GearSlot.CHEST.ordinal();
            if (index == chestGearSlotIndex) {
                ((IHorseData) this.horse).bh_onChestGearRemoved(copiedStack);
            }
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        cir.setReturnValue(copiedStack);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void bh_onMenuRemoved(Player player, CallbackInfo ci) {
        // Persist the ender chest view back to the real ender chest if it was active.
        this.bh_saveEnderChestView();
    }

    @Unique
    private boolean bh_moveIntoFirstMatchingGearSlot(ItemStack stack) {
        for (int slotIndex = bh_gearStartIndex; slotIndex < bh_chestStartIndex; slotIndex++) {
            Slot slot = this.slots.get(slotIndex);
            if (!slot.isActive() || slot.hasItem() || !slot.mayPlace(stack)) {
                continue;
            }

            if (this.moveItemStackTo(stack, slotIndex, slotIndex + 1, false)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void bh_refreshLayout() {
        if (this.bh_playerInventoryStartIndex < 0 || this.bh_playerInventoryEndIndex < 0) {
            return;
        }

        boolean shouldShiftPlayerInventory = this.bh_hasChestStorageLayout();
        if (shouldShiftPlayerInventory == this.bh_playerInventoryShifted) {
            return;
        }

        int offset = shouldShiftPlayerInventory ? BH_PLAYER_SLOT_Y_OFFSET : -BH_PLAYER_SLOT_Y_OFFSET;
        for (int slotIndex = this.bh_playerInventoryStartIndex; slotIndex < this.bh_playerInventoryEndIndex; slotIndex++) {
            Slot slot = this.slots.get(slotIndex);
            ((SlotAccessor) slot).bh_setY(slot.y + offset);
        }

        this.bh_playerInventoryShifted = shouldShiftPlayerInventory;
    }

    @Override
    public boolean bh_hasUpgradedSaddleLayout() {
        return this.bh_hasUpgradedSaddleInMenu();
    }

    @Override
    public boolean bh_hasChestStorageLayout() {
        return this.bh_hasUpgradedSaddleInMenu() && this.bh_hasChestGearInMenu();
    }

    @Override
    public int bh_getGearStartIndex() {
        return this.bh_gearStartIndex;
    }

    @Override
    public int bh_getChestStartIndex() {
        return this.bh_chestStartIndex;
    }

    @Unique
    private boolean bh_hasUpgradedSaddleInMenu() {
        return this.getSlot(0).getItem().is(ModItems.UPGRADED_SADDLE.get());
    }

    @Unique
    private boolean bh_hasChestGearInMenu() {
        int chestGearSlotIndex = bh_gearStartIndex + GearSlot.CHEST.ordinal();
        if (bh_gearStartIndex < 0 || chestGearSlotIndex >= this.slots.size()) {
            return false;
        }
        return this.bh_isChestGear(this.slots.get(chestGearSlotIndex).getItem());
    }

    @Unique
    boolean bh_isChestGear(ItemStack stack) {
        return this.bh_isStorageChestGear(stack) || this.bh_isEnderChestGear(stack);
    }

    @Unique
    private boolean bh_isStorageChestGear(ItemStack stack) {
        return stack.is(Items.CHEST);
    }

    @Unique
    boolean bh_isEnderChestGear(ItemStack stack) {
        return stack.is(Items.ENDER_CHEST);
    }

    @Unique
    private void bh_handleChestGearChange(ItemStack previousStack, ItemStack newStack, IHorseData data) {
        boolean wasStorageChest = this.bh_isStorageChestGear(previousStack);
        boolean isStorageChest = this.bh_isStorageChestGear(newStack);
        boolean wasEnderChest = this.bh_isEnderChestGear(previousStack);
        boolean isEnderChest = this.bh_isEnderChestGear(newStack);

        if (wasStorageChest && !isStorageChest) {
            data.bh_onChestGearRemoved(previousStack);
        }
        if (wasEnderChest && !isEnderChest) {
            this.bh_saveEnderChestView();
        }
        if (!wasEnderChest && isEnderChest) {
            this.bh_loadEnderChestView();
        }
    }

    @Unique
    private void bh_loadEnderChestView() {
        this.bh_enderChestView.clearContent();
        if (this.bh_playerEnderChest == null) {
            this.bh_enderChestViewLoaded = false;
            return;
        }

        for (int slot = 0; slot < BH_CHEST_SLOT_COUNT; slot++) {
            this.bh_enderChestView.setItem(slot, this.bh_playerEnderChest.getItem(slot).copy());
        }
        this.bh_enderChestViewLoaded = true;
    }

    @Unique
    private void bh_saveEnderChestView() {
        if (this.bh_playerEnderChest == null || !this.bh_enderChestViewLoaded) {
            return;
        }

        for (int slot = 0; slot < BH_CHEST_SLOT_COUNT; slot++) {
            this.bh_playerEnderChest.setItem(slot, this.bh_enderChestView.getItem(slot).copy());
        }
        this.bh_playerEnderChest.setChanged();
        this.bh_enderChestViewLoaded = false;
    }
}
