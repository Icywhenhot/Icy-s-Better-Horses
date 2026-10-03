package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnimalArmorItem.class)
public abstract class AnimalArmorItemMixin {

    @Shadow public abstract AnimalArmorItem.BodyType getBodyType();

    @ModifyReturnValue(method = "isEnchantable", at = @At("RETURN"))
    private boolean bh_enchantHorseArmor(boolean enchantable, ItemStack stack) {
        return enchantable || getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN;
    }
}
