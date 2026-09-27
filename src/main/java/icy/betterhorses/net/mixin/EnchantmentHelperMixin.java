package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getMobLooting", at = @At("RETURN"), cancellable = true)
    private static void bh_paintLooting(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof Player) || !(entity.getVehicle() instanceof AbstractHorse horse)
                || !BhHorseKind.managed(horse)) {
            return;
        }
        IHorseData d = IHorseData.of(horse);
        if (!Objects.equals(d.bh_getBreedKey(), BhContent.AMERICAN_PAINT.key()) || !BhAbility.PAINT_LOOTING.on()) {
            return;
        }
        cir.setReturnValue(Math.max(cir.getReturnValueI(), BhHorseTraits.bondTier(d.bh_getBond()) + 1));
    }
}
