package icy.betterhorses.net.inventory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.function.BooleanSupplier;

public class RiderGearSlot extends Slot {

    private final Player owner;
    private final EquipmentSlot type;
    private final Identifier icon;
    private final BooleanSupplier shown;

    public RiderGearSlot(Player owner, EquipmentSlot type, int index, int x, int y, Identifier icon, BooleanSupplier shown) {
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
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        this.owner.onEquipItem(this.type, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
    }

    @Override
    public int getMaxStackSize() {
        return this.type == EquipmentSlot.OFFHAND ? super.getMaxStackSize() : 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.type == EquipmentSlot.OFFHAND || this.owner.isEquippableInSlot(stack, this.type);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack held = this.getItem();
        if (this.type != EquipmentSlot.OFFHAND && !held.isEmpty() && !player.isCreative()
                && EnchantmentHelper.has(held, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Override
    public Identifier getNoItemIcon() {
        return this.icon;
    }
}
