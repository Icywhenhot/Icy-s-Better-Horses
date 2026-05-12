package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractMountInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.21.11 moved mount shift-click logic up from {@code HorseInventoryMenu} to
 * {@link AbstractMountInventoryMenu}. Keep the upgraded-saddle gear/chest routes wired in there.
 */
@Mixin(AbstractMountInventoryMenu.class)
public abstract class AbstractMountInventoryMenuMixin extends AbstractContainerMenu {

    @Shadow @Final protected Container mountContainer;
    @Shadow @Final protected LivingEntity mount;

    protected AbstractMountInventoryMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void bh_onMenuRemoved(Player player, CallbackInfo ci) {
        if (this instanceof HorseInventoryLayoutAccess layoutAccess) {
            layoutAccess.bh_onMenuRemoved(player);
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void bh_quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (!(this instanceof HorseInventoryLayoutAccess layoutAccess)
                || !(this.mount instanceof AbstractHorse horse)) {
            return;
        }

        int gearStartIndex = layoutAccess.bh_getGearStartIndex();
        int chestStartIndex = layoutAccess.bh_getChestStartIndex();
        if (index < 0 || index >= this.slots.size() || gearStartIndex < 0 || chestStartIndex < 0) {
            return;
        }

        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copiedStack = sourceStack.copy();

        int mountSlotEnd = this.mountContainer.getContainerSize() + 2;
        int playerInventoryStart = mountSlotEnd;
        int playerInventoryEnd = playerInventoryStart + 27;
        int hotbarStart = playerInventoryEnd;
        int hotbarEnd = hotbarStart + 9;

        boolean moved;
        if (index < mountSlotEnd || index >= gearStartIndex) {
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
                moved = this.bh_moveIntoFirstMatchingGearSlot(sourceStack, gearStartIndex, chestStartIndex);
                if (!moved && this.bh_hasChestGearInMenu(gearStartIndex)) {
                    moved = this.moveItemStackTo(sourceStack, chestStartIndex, chestStartIndex + 27, false);
                }
            }

            if (!moved && mountSlotEnd > 2) {
                moved = this.moveItemStackTo(sourceStack, 2, mountSlotEnd, false);
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
            int chestGearSlotIndex = gearStartIndex + GearSlot.CHEST.ordinal();
            if (index == chestGearSlotIndex) {
                ((IHorseData) horse).bh_onChestGearRemoved(copiedStack);
            }
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        cir.setReturnValue(copiedStack);
    }

    @Unique
    private boolean bh_moveIntoFirstMatchingGearSlot(ItemStack stack, int gearStartIndex, int chestStartIndex) {
        for (int slotIndex = gearStartIndex; slotIndex < chestStartIndex; slotIndex++) {
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

    @Unique
    private boolean bh_hasUpgradedSaddleInMenu() {
        return this.getSlot(0).getItem().is(ModItems.UPGRADED_SADDLE);
    }

    @Unique
    private boolean bh_hasChestGearInMenu(int gearStartIndex) {
        int chestGearSlotIndex = gearStartIndex + GearSlot.CHEST.ordinal();
        if (chestGearSlotIndex >= this.slots.size()) {
            return false;
        }

        ItemStack chestStack = this.slots.get(chestGearSlotIndex).getItem();
        return chestStack.is(net.minecraft.world.item.Items.CHEST)
                || chestStack.is(net.minecraft.world.item.Items.ENDER_CHEST);
    }
}
