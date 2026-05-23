package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import icy.betterhorses.net.ModItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Allows the upgraded saddle to be placed into the vanilla horse-inventory saddle slot
 * (drag-and-drop equip). Vanilla's saddle slot is an anonymous inner class in
 * {@link net.minecraft.world.inventory.HorseInventoryMenu} (the first one declared, hence
 * {@code $1}). Its {@code mayPlace} returns {@code stack.is(Items.SADDLE)} — we OR in a check
 * for our upgraded saddle so both vanilla and modded saddles are accepted.
 */
@Mixin(targets = "net.minecraft.world.inventory.HorseInventoryMenu$1")
public abstract class HorseSaddleSlotMixin {

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean bh_allowUpgradedSaddle(boolean original, ItemStack stack) {
        return original || stack.is(ModItems.UPGRADED_SADDLE.get());
    }
}
