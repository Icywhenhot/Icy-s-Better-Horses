package icy.betterhorses.net.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.function.BooleanSupplier;

public class RiderGearSlot extends Slot {

    private final Player owner;
    private final EquipmentSlot type;
    private final ResourceLocation icon;
    private final BooleanSupplier shown;

    public RiderGearSlot(Player owner, EquipmentSlot type, int index, int x, int y, ResourceLocation icon, BooleanSupplier shown) {
        super(owner.getInventory(), index, x, y);
        this.owner = owner;
        this.type = type;
        this.icon = icon;
        this.shown = shown;
    }

    public EquipmentSlot type() {
        return this.type;
    }

    @Override
    public boolean isActive() {
        return this.shown.getAsBoolean();
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        if (Equipable.get(stack) != null) {
            this.owner.onEquipItem(this.type, this.getItem(), stack);
        }
        super.setByPlayer(stack);
    }

    @Override
    public int getMaxStackSize() {
        return this.type == EquipmentSlot.OFFHAND ? super.getMaxStackSize() : 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.type == EquipmentSlot.OFFHAND || stack.canEquip(this.type, this.owner);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack held = this.getItem();
        if (this.type != EquipmentSlot.OFFHAND && !held.isEmpty() && !player.isCreative()
                && EnchantmentHelper.hasBindingCurse(held)) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, this.icon);
    }
}
