package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin extends AbstractContainerMenu implements HorseInventoryLayoutAccess {

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
        this.bh_playerInventoryStartIndex = horseContainer.getContainerSize() + 2;
        this.bh_playerInventoryEndIndex = Math.min(this.bh_playerInventoryStartIndex + 36, this.slots.size());

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

        this.bh_refreshLayout();
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
    private boolean bh_isChestGear(ItemStack stack) {
        return stack.is(Items.CHEST);
    }

    @Unique
    private boolean bh_hasUpgradedSaddleInMenu() {
        return this.getSlot(0).getItem().is(icy.betterhorses.net.ModItems.UPGRADED_SADDLE);
    }

    @Unique
    private boolean bh_hasChestGearInMenu() {
        int chestGearSlotIndex = bh_gearStartIndex + GearSlot.CHEST.ordinal();
        if (bh_gearStartIndex < 0 || chestGearSlotIndex >= this.slots.size()) {
            return false;
        }
        return this.bh_isChestGear(this.slots.get(chestGearSlotIndex).getItem());
    }
}
