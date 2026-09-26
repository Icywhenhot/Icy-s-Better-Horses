package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
    private void bh_frostWalkerHooves(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && stack.is(ModItems.HORSE_HOOVES) && (Object) this == Enchantments.FROST_WALKER) {
            cir.setReturnValue(true);
        }
    }
}
