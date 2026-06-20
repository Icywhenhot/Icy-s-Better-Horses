package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import icy.betterhorses.net.ModItems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Lets the upgraded saddle drop into vanilla's saddle slot (HorseInventoryMenu$1.mayPlace).
@Mixin(targets = "net.minecraft.world.inventory.HorseInventoryMenu$1")
public abstract class HorseSaddleSlotMixin {

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean bh_allowUpgradedSaddle(boolean original, ItemStack stack) {
        return original || stack.is(ModItems.UPGRADED_SADDLE.get());
    }
}
