package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @ModifyReturnValue(method = "getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("RETURN"))
    private static int bh_paintLooting(int level, Holder<Enchantment> enchantment, LivingEntity entity) {
        if (!(entity instanceof Player) || !enchantment.is(Enchantments.LOOTING)
                || !(entity.getVehicle() instanceof AbstractHorse horse) || !BhHorseKind.managed(horse)) {
            return level;
        }
        IHorseData data = IHorseData.of(horse);
        if (!Objects.equals(data.bh_getBreedKey(), BhContent.AMERICAN_PAINT.key())
                || !BhAbility.PAINT_LOOTING.on()) {
            return level;
        }
        return Math.max(level, BhHorseTraits.bondTier(data.bh_getBond()) + 1);
    }
}
