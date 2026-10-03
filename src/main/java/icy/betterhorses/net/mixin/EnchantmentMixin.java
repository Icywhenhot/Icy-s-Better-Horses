package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Shadow public abstract Enchantment.EnchantmentDefinition definition();

    @ModifyReturnValue(method = "matchingSlot", at = @At("RETURN"))
    private boolean bh_enchantBodyArmor(boolean matches, EquipmentSlot slot) {
        if (matches || slot != EquipmentSlot.BODY
                || definition().supportedItems().stream().noneMatch(item -> item.value() instanceof AnimalArmorItem armor
                        && armor.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN)) {
            return matches;
        }
        return definition().slots().stream().anyMatch(group -> group.test(EquipmentSlot.HEAD)
                || group.test(EquipmentSlot.CHEST) || group.test(EquipmentSlot.LEGS) || group.test(EquipmentSlot.FEET));
    }
}
