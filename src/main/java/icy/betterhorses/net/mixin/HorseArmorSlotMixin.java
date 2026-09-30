package icy.betterhorses.net.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.world.inventory.HorseInventoryMenu$2")
public abstract class HorseArmorSlotMixin extends Slot {

    protected HorseArmorSlotMixin(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        return player.isCreative() || !EnchantmentHelper.hasBindingCurse(this.getItem());
    }
}
