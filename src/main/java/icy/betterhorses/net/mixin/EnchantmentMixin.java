package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.feature.ArmorEnchants;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin {

    @Inject(method = "canEnchant", at = @At("RETURN"), cancellable = true)
    private void bh_gearEnchants(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof HorseArmorItem) {
            cir.setReturnValue(ArmorEnchants.accepts((Enchantment) (Object) this));
        }
        if (!cir.getReturnValueZ() && stack.is(ModItems.HORSE_HOOVES) && (Object) this == Enchantments.FROST_WALKER) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getSlotItems", at = @At("RETURN"))
    private void bh_enchantHorseArmor(LivingEntity entity, CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (entity instanceof Horse horse && horse.getArmor().getItem() instanceof HorseArmorItem
                && ArmorEnchants.accepts((Enchantment) (Object) this)) {
            cir.getReturnValue().put(EquipmentSlot.CHEST, horse.getArmor());
        }
    }
}
