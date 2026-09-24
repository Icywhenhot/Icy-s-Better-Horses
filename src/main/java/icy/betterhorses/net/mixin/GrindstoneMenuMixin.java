package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {

    @Inject(method = "mergeItems", at = @At("HEAD"), cancellable = true)
    private void bh_noHarnessMerge(ItemStack input, ItemStack additional, CallbackInfoReturnable<ItemStack> cir) {
        if (input.is(ModItems.HORSE_STABILIZER)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
