package icy.betterhorses.net.inventory;

import com.mojang.datafixers.util.Pair;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModMenus;
import icy.betterhorses.net.entity.CartType;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.mixin.SlotAccessor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CartMenu extends AbstractContainerMenu {

    public static final int TAB_CARGO = 0;
    public static final int TAB_CART = 1;
    public static final int TAB_RIDERS = 2;

    public static final int BUTTON_PART = 16;
    public static final int BUTTON_PICKUP = 24;
    public static final int BUTTON_UNLOAD = 28;
    public static final int BUTTON_TAB = 32;

    public static final int HEIGHT = 222;
    public static final int RIG_Y = 85;
    public static final int RIG_PITCH = 20;

    public static final int RIG_START = CartType.CHEST_SLOTS;
    private static final int PLAYER_START = RIG_START + CartType.Attachment.values().length;

    private static final ResourceLocation EMPTY_CHEST =
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "item/empty_slot_chest");
    private static final ResourceLocation EMPTY_HOE = new ResourceLocation("item/empty_slot_hoe");

    private final @Nullable HorseCartEntity cart;
    private final Container chest;
    private int tab;
    private @Nullable CartType laidOut;

    public CartMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                playerInventory.player.level().getEntity(buf.readVarInt()) instanceof HorseCartEntity found ? found : null,
                new SimpleContainer(CartType.CHEST_SLOTS),
                new SimpleContainer(CartType.Attachment.values().length),
                buf.readByte());
    }

    public CartMenu(int containerId, Inventory playerInventory, HorseCartEntity cart, Container chest, int tab) {
        this(containerId, playerInventory, cart, chest, cart.rig(), tab);
    }

    private CartMenu(int containerId, Inventory playerInventory, @Nullable HorseCartEntity cart,
                     Container chest, Container rig, int tab) {
        super(ModMenus.CART, containerId);
        checkContainerSize(chest, CartType.CHEST_SLOTS);
        this.cart = cart;
        this.chest = chest;
        this.tab = tab;

        for (int i = 0; i < CartType.CHEST_SLOTS; i++) {
            this.addSlot(new CargoSlot(chest, i));
        }
        for (CartType.Attachment kind : CartType.Attachment.values()) {
            this.addSlot(new RigSlot(rig, kind));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 0, 0));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 0, 0));
        }
        this.layout();
    }

    public static int width(CartType type) {
        return type.chestColumns() * 18 + 14;
    }

    public @Nullable HorseCartEntity cart() {
        return this.cart;
    }

    public CartType type() {
        return this.cart == null ? CartType.BUGGY : this.cart.type();
    }

    public Container chest() {
        return this.chest;
    }

    public int tab() {
        return this.tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public boolean needsLayout() {
        return this.laidOut != this.type();
    }

    public void layout() {
        CartType type = this.type();
        this.laidOut = type;
        int width = width(type);
        for (int i = 0; i < CartType.CHEST_SLOTS; i++) {
            place(i, 8 + (i % CartType.CHEST_COLUMNS) * 18, 18 + (i / CartType.CHEST_COLUMNS) * 18);
        }
        List<CartType.Attachment> fitted = type.attachments();
        int left = width / 2 - (RIG_PITCH * (fitted.size() - 1) + 18) / 2;
        for (CartType.Attachment kind : CartType.Attachment.values()) {
            int at = fitted.indexOf(kind);
            place(RIG_START + kind.ordinal(), at < 0 ? -2000 : left + at * RIG_PITCH + 1, RIG_Y + 1);
        }
        int invLeft = (width - 162) / 2 + 1;
        for (int i = 0; i < 36; i++) {
            int row = i / 9;
            place(PLAYER_START + i, invLeft + (i % 9) * 18, row < 3 ? 140 + row * 18 : 198);
        }
    }

    private void place(int index, int x, int y) {
        SlotAccessor slot = (SlotAccessor) this.slots.get(index);
        slot.bh_setX(x);
        slot.bh_setY(y);
    }

    public boolean chestEmpty() {
        for (int i = 0; i < CartType.CHEST_SLOTS; i++) {
            if (!this.chest.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.cart == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (id >= BUTTON_TAB && id <= BUTTON_TAB + TAB_RIDERS) {
            this.tab = id - BUTTON_TAB;
        } else if (id < BUTTON_PART) {
            if (id < CartType.values().length) {
                this.cart.switchType(serverPlayer, CartType.values()[id]);
            }
        } else if (id < BUTTON_PICKUP) {
            this.cart.togglePart(id - BUTTON_PART);
        } else if (id == BUTTON_PICKUP) {
            this.cart.togglePickup(HorseCartEntity.PICKUP_ON);
        } else if (id < BUTTON_UNLOAD) {
            this.cart.togglePickup(HorseCartEntity.PICKUP_PASSIVE << (id - BUTTON_PICKUP - 1));
        } else if (id == BUTTON_UNLOAD) {
            this.cart.unloadPassengers();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.cart != null && this.cart.mayKeepMenuOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < PLAYER_START) {
            if (!this.moveItemStackTo(stack, PLAYER_START, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (this.tab == TAB_CARGO) {
            if (!this.moveItemStackTo(stack, 0, RIG_START, false)) {
                return ItemStack.EMPTY;
            }
        } else if (this.tab == TAB_CART) {
            Slot free = null;
            for (int i = RIG_START; i < PLAYER_START; i++) {
                Slot rig = this.slots.get(i);
                if (!rig.hasItem() && rig.mayPlace(stack)) {
                    free = rig;
                    break;
                }
            }
            if (free == null) {
                return ItemStack.EMPTY;
            }
            free.setByPlayer(stack.split(1));
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private boolean showing(int wanted) {
        return this.tab == wanted;
    }

    private final class CargoSlot extends Slot {

        CargoSlot(Container container, int index) {
            super(container, index, 0, 0);
        }

        private boolean open() {
            return cart != null && cart.hasChest()
                    && this.getContainerSlot() % CartType.CHEST_COLUMNS < type().chestColumns();
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.open();
        }

        @Override
        public boolean isActive() {
            return showing(TAB_CARGO) && this.open();
        }
    }

    public final class RigSlot extends Slot {

        private final CartType.Attachment kind;

        RigSlot(Container container, CartType.Attachment kind) {
            super(container, kind.ordinal(), 0, 0);
            this.kind = kind;
        }

        public CartType.Attachment kind() {
            return this.kind;
        }

        private boolean offered() {
            return cart != null && type().attachments().contains(this.kind);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (!this.offered()) {
                return false;
            }
            return this.kind == CartType.Attachment.CHEST
                    ? GearSlot.isStorageChest(stack) && !cart.hasChest()
                    : stack.is(ItemTags.HOES) && !cart.hasPlough();
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.kind != CartType.Attachment.CHEST || chestEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean isActive() {
            return showing(TAB_CART) && this.offered();
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS,
                    this.kind == CartType.Attachment.CHEST ? EMPTY_CHEST : EMPTY_HOE);
        }
    }
}
