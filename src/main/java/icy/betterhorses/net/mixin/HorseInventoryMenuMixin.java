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
    @Unique private static final int BH_VANILLA_PLAYER_INV_Y = 84;
    @Unique private static final int BH_VANILLA_HOTBAR_Y = 142;
    @Unique private static final int BH_PLAYER_INV_Y_SHIFT = 54;

    @Unique private int bh_gearStartIndex = -1;
    @Unique private int bh_chestStartIndex = -1;

    protected HorseInventoryMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bh_appendUpgradedSaddleSlots(
            int containerId,
            Inventory playerInventory,
            Container horseContainer,
            AbstractHorse horse,
            int horseChestColumns,
            CallbackInfo ci) {
        final IHorseData data = (IHorseData) horse;
        final SimpleContainer gear = data.bh_getGearContainer();
        final SimpleContainer chest = data.bh_getChestContainer();

        this.bh_gearStartIndex = this.slots.size();
        for (GearSlot slot : GearSlot.values()) {
            final GearSlot type = slot;
            this.addSlot(new Slot(gear, slot.ordinal(), BH_GEAR_SLOT_X + slot.ordinal() * 18, BH_GEAR_SLOT_Y) {
                @Override public boolean mayPlace(ItemStack stack) { return type.accepts(stack); }
                @Override public boolean isActive() { return HorseInventoryMenuMixin.this.bh_hasUpgradedSaddleInMenu(); }
                @Override public int getMaxStackSize() { return 1; }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    super.onTake(player, stack);
                    if (type == GearSlot.CHEST) {
                        data.bh_onChestGearRemoved(stack);
                    }
                }
            });
        }

        this.bh_chestStartIndex = this.slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                final int index = col + row * 9;
                this.addSlot(new Slot(chest, index, BH_CHEST_SLOT_X + col * 18, BH_CHEST_SLOT_Y + row * 18) {
                    @Override
                    public boolean isActive() {
                        return HorseInventoryMenuMixin.this.bh_hasUpgradedSaddleInMenu()
                                && HorseInventoryMenuMixin.this.bh_isChestGear(gear.getItem(GearSlot.CHEST.ordinal()));
                    }
                });
            }
        }
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

        int horseSlotEnd = 2 + this.horseContainer.getContainerSize();
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
        bh_updatePlayerInventorySlotY();
    }

    @Unique
    private void bh_updatePlayerInventorySlotY() {
        if (this.bh_gearStartIndex < 0) {
            return;
        }
        int playerStart = 2 + this.horseContainer.getContainerSize();
        if (playerStart + 36 > this.bh_gearStartIndex) {
            return;
        }

        int yOffset = this.bh_hasChestStorageLayout() ? BH_PLAYER_INV_Y_SHIFT : 0;

        for (int i = 0; i < 27; i++) {
            Slot s = this.slots.get(playerStart + i);
            int targetY = BH_VANILLA_PLAYER_INV_Y + (i / 9) * 18 + yOffset;
            if (s.y != targetY) {
                ((SlotAccessor) s).bh_setY(targetY);
            }
        }
        for (int i = 0; i < 9; i++) {
            Slot s = this.slots.get(playerStart + 27 + i);
            int targetY = BH_VANILLA_HOTBAR_Y + yOffset;
            if (s.y != targetY) {
                ((SlotAccessor) s).bh_setY(targetY);
            }
        }
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
        return this.getSlot(0).getItem().is(ModItems.UPGRADED_SADDLE);
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
    private boolean bh_isChestGear(ItemStack stack) {
        return stack.is(Items.CHEST);
    }
}
