package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import icy.betterhorses.net.inventory.RiderGearSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import icy.betterhorses.net.BhCriteria;
import icy.betterhorses.net.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.Horse;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(HorseInventoryMenu.class)
public abstract class HorseInventoryMenuMixin extends AbstractContainerMenu implements HorseInventoryLayoutAccess {

    @Unique private static final int BH_GEAR_SLOT_X = 80;
    @Unique private static final int BH_GEAR_SLOT_Y = 18;
    @Unique private static final int BH_CHEST_SLOT_X = 8;
    @Unique private static final int BH_CHEST_SLOT_Y = 79;
    @Unique private static final int BH_ENDER_SLOT_COUNT = 27;
    @Unique private static final int BH_MAX_CHEST_ROWS = 6;
    @Unique private static final int BH_ROW_HEIGHT = 18;
    @Unique private static final int BH_CHEST_GAP = 7;
    @Unique private static final int BH_OFFSCREEN = 10000;
    @Unique private static final EquipmentSlot[] BH_RIDER_ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    @Unique private static final ResourceLocation[] BH_RIDER_ARMOR_ICONS = {
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS};

    @Unique private int bh_gearStartIndex = -1;
    @Unique private int bh_chestStartIndex = -1;
    @Unique private int bh_riderGearStartIndex = -1;
    @Unique private boolean bh_riderPanel = false;
    @Unique private int bh_playerInventoryStartIndex = -1;
    @Unique private int bh_playerInventoryEndIndex = -1;
    @Unique private int bh_appliedShift = 0;
    @Unique private int bh_chestRows = 3;
    @Unique private @Nullable AbstractHorse bh_horse = null;
    @Unique private @Nullable SimpleContainer bh_gearContainer = null;
    @Unique private final SimpleContainer bh_enderChestView = new SimpleContainer(BH_ENDER_SLOT_COUNT);
    @Unique private PlayerEnderChestContainer bh_playerEnderChest = null;
    @Unique private @Nullable Player bh_menuPlayer = null;

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
        Slot saddle = new Slot(horseContainer, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return (stack.is(Items.SADDLE) || stack.is(ModItems.UPGRADED_SADDLE))
                        && !hasItem() && horse.isSaddleable();
            }

            @Override
            public boolean mayPickup(Player player) {
                return !IHorseData.of(horse).bh_hasCartGear() && super.mayPickup(player);
            }

            @Override
            public boolean isActive() {
                return horse.isSaddleable();
            }
        };
        saddle.index = 0;
        this.slots.set(0, saddle);
        final IHorseData data = IHorseData.of(horse);
        this.bh_horse = horse;
        final SimpleContainer gear = data.bh_getGearContainer();
        this.bh_gearContainer = gear;
        this.bh_chestRows = this.bh_resolveChestRows();
        final SimpleContainer chest = data.bh_getChestContainer();
        this.bh_menuPlayer = playerInventory.player;
        this.bh_playerEnderChest = playerInventory.player.level().isClientSide()
                ? null
                : playerInventory.player.getEnderChestInventory();
        if (this.bh_isEnderChestGear(gear.getItem(GearSlot.CHEST.ordinal()))) {
            if (playerInventory.player instanceof ServerPlayer serverPlayer) {
                BhCriteria.fire(serverPlayer, BhCriteria.ENDER_CHEST_GEAR);
            }
        }
        final Container extraStorage = new Container() {
            private Container bh_active() {
                return HorseInventoryMenuMixin.this.bh_isEnderChestGear(gear.getItem(GearSlot.CHEST.ordinal()))
                        ? (HorseInventoryMenuMixin.this.bh_playerEnderChest == null
                                ? HorseInventoryMenuMixin.this.bh_enderChestView
                                : HorseInventoryMenuMixin.this.bh_playerEnderChest)
                        : chest;
            }

            @Override
            public int getContainerSize() {
                return BH_MAX_CHEST_ROWS * 9;
            }

            private boolean bh_holds(int slot) {
                return slot >= 0 && slot < this.bh_active().getContainerSize();
            }

            @Override
            public boolean isEmpty() {
                return this.bh_active().isEmpty();
            }

            @Override
            public ItemStack getItem(int slot) {
                return this.bh_holds(slot) ? this.bh_active().getItem(slot) : ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                return this.bh_holds(slot) ? this.bh_active().removeItem(slot, amount) : ItemStack.EMPTY;
            }

            @Override
            public ItemStack removeItemNoUpdate(int slot) {
                return this.bh_holds(slot) ? this.bh_active().removeItemNoUpdate(slot) : ItemStack.EMPTY;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                if (this.bh_holds(slot)) {
                    this.bh_active().setItem(slot, stack);
                }
            }

            @Override
            public void setChanged() {
                this.bh_active().setChanged();
            }

            @Override
            public boolean stillValid(Player player) {
                return this.bh_active().stillValid(player);
            }

            @Override
            public void startOpen(Player user) {
                this.bh_active().startOpen(user);
            }

            @Override
            public void stopOpen(Player user) {
                this.bh_active().stopOpen(user);
            }

            @Override
            public void clearContent() {
                this.bh_active().clearContent();
            }
        };
        this.bh_playerInventoryStartIndex = horseContainer.getContainerSize();
        this.bh_playerInventoryEndIndex = Math.min(this.bh_playerInventoryStartIndex + 36, this.slots.size());

        this.bh_gearStartIndex = this.slots.size();
        for (GearSlot slot : GearSlot.values()) {
            final GearSlot type = slot;
            this.addSlot(new Slot(gear, slot.ordinal(), BH_GEAR_SLOT_X + slot.ordinal() * 18, BH_GEAR_SLOT_Y) {
                @Override public boolean mayPlace(ItemStack stack) {
                    if (type == GearSlot.STABILIZER
                            && HorseInventoryMenuMixin.this.bh_isCartSlotLocked()) {
                        return false;
                    }
                    if (type == GearSlot.STABILIZER
                            && stack.is(ModItems.HORSE_STABILIZER)
                            && !(horse instanceof Horse)) {
                        return false;
                    }
                    return type.accepts(stack);
                }

                @Override public boolean mayPickup(Player player) {
                    return !(type == GearSlot.STABILIZER
                            && HorseInventoryMenuMixin.this.bh_isCartSlotLocked());
                }
                @Override public boolean isActive() {
                    return !HorseInventoryMenuMixin.this.bh_riderPanel
                            && HorseInventoryMenuMixin.this.bh_hasUpgradedSaddleInMenu();
                }
                @Override public int getMaxStackSize() { return 1; }

                @Override
                public void set(ItemStack stack) {
                    ItemStack previousStack = this.getItem().copy();
                    super.set(stack);
                    if (type == GearSlot.CHEST) {
                        HorseInventoryMenuMixin.this.bh_handleChestGearChange(previousStack, stack, data);
                    }
                    if (type == GearSlot.CHEST) {
                        HorseInventoryMenuMixin.this.bh_refreshLayout();
                    }
                }

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
        for (int row = 0; row < BH_MAX_CHEST_ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                final int chestSlot = col + row * 9;
                this.addSlot(new Slot(extraStorage, chestSlot, BH_CHEST_SLOT_X + col * 18, BH_CHEST_SLOT_Y + row * 18) {
                    @Override
                    public boolean isActive() {
                        return chestSlot < HorseInventoryMenuMixin.this.bh_chestRows * 9
                                && HorseInventoryMenuMixin.this.bh_hasUpgradedSaddleInMenu()
                                && HorseInventoryMenuMixin.this.bh_isChestGear(gear.getItem(GearSlot.CHEST.ordinal()));
                    }
                });
            }
        }

        this.bh_riderGearStartIndex = this.slots.size();
        Player rider = playerInventory.player;
        for (int i = 0; i < BH_RIDER_ARMOR.length; i++) {
            EquipmentSlot type = BH_RIDER_ARMOR[i];
            this.addSlot(new RiderGearSlot(rider, type, Inventory.INVENTORY_SIZE + type.getIndex(),
                    BH_GEAR_SLOT_X + i * 18, BH_GEAR_SLOT_Y, BH_RIDER_ARMOR_ICONS[i], () -> this.bh_riderPanel));
        }
        this.addSlot(new RiderGearSlot(rider, EquipmentSlot.OFFHAND, Inventory.SLOT_OFFHAND,
                8, 18, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD, () -> this.bh_riderPanel));

        this.bh_refreshLayout();
    }

    @Override
    public boolean bh_isRiderPanel() {
        return this.bh_riderPanel;
    }

    @Override
    public void bh_setRiderPanel(boolean shown) {
        if (shown == this.bh_riderPanel) {
            return;
        }
        this.bh_riderPanel = shown;
        int dx = shown ? BH_OFFSCREEN : -BH_OFFSCREEN;
        for (int i = 0; i < this.bh_playerInventoryStartIndex; i++) {
            Slot slot = this.slots.get(i);
            ((SlotAccessor) slot).bh_setX(slot.x + dx);
        }
    }

    @Override
    public int bh_getRiderGearStartIndex() {
        return this.bh_riderGearStartIndex;
    }

    @Override
    public @Nullable AbstractHorse bh_mount() {
        return this.bh_horse;
    }

    @Override
    public void bh_refreshLayout() {
        this.bh_chestRows = this.bh_resolveChestRows();
        if (this.bh_playerInventoryStartIndex < 0 || this.bh_playerInventoryEndIndex < 0) {
            return;
        }

        int wanted = this.bh_hasChestStorageLayout() ? this.bh_chestRows * BH_ROW_HEIGHT + BH_CHEST_GAP : 0;
        if (wanted == this.bh_appliedShift) {
            return;
        }

        int delta = wanted - this.bh_appliedShift;
        for (int slotIndex = this.bh_playerInventoryStartIndex; slotIndex < this.bh_playerInventoryEndIndex; slotIndex++) {
            Slot slot = this.slots.get(slotIndex);
            ((SlotAccessor) slot).bh_setY(slot.y + delta);
        }

        this.bh_appliedShift = wanted;
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
    public boolean bh_isCartSlotLocked() {
        return this.bh_horse != null
                && IHorseData.of(this.bh_horse).bh_hasCartGear()
                && IHorseData.of(this.bh_horse).bh_hasCartChest();
    }

    @Override
    public boolean bh_isSaddleSlotLocked() {
        return this.bh_horse != null && IHorseData.of(this.bh_horse).bh_hasCartGear();
    }

    @Override
    public int bh_getChestRows() {
        this.bh_chestRows = this.bh_resolveChestRows();
        return this.bh_chestRows;
    }

    @Unique
    private int bh_resolveChestRows() {
        if (this.bh_horse == null) {
            return this.bh_chestRows;
        }
        int rows = IHorseData.of(this.bh_horse).bh_getChestRows();
        if (this.bh_gearContainer != null
                && this.bh_isEnderChestGear(this.bh_gearContainer.getItem(GearSlot.CHEST.ordinal()))) {
            rows = Math.min(rows, BH_ENDER_SLOT_COUNT / 9);
        } else {
            rows = Math.min(rows, IHorseData.of(this.bh_horse).bh_getChestContainer().getContainerSize() / 9);
        }
        return rows;
    }

    @Override
    public List<Storage> bh_storage() {
        List<Storage> out = new ArrayList<>();
        if (this.bh_hasChestStorageLayout()) {
            out.add(new Storage(this.slots.get(this.bh_chestStartIndex).container, 0, this.bh_getChestRows() * 9));
        }
        Container inv = this.slots.get(0).container;
        if (inv.getContainerSize() > AbstractHorse.INV_BASE_COUNT) {
            out.add(new Storage(inv, AbstractHorse.INV_BASE_COUNT, inv.getContainerSize()));
        }
        return out;
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
        return this.bh_isStorageChestGear(stack) || this.bh_isEnderChestGear(stack);
    }

    @Unique
    private boolean bh_isStorageChestGear(ItemStack stack) {
        return GearSlot.isStorageChest(stack);
    }

    @Unique
    private boolean bh_isEnderChestGear(ItemStack stack) {
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
        if (!wasEnderChest && isEnderChest) {
            if (this.bh_menuPlayer instanceof ServerPlayer serverPlayer) {
                BhCriteria.fire(serverPlayer, BhCriteria.ENDER_CHEST_GEAR);
            }
        }
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
}
