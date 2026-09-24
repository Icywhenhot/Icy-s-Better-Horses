package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {

    @Inject(method = "canCombine", at = @At("HEAD"), cancellable = true)
    private static void bh_noHarnessCombine(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (first.is(ModItems.HORSE_STABILIZER)) {
            cir.setReturnValue(false);
        }
    }
}
