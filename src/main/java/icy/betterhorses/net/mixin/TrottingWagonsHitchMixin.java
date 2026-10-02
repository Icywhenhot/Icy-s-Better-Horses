package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.IHorseData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(targets = "net.favouriteless.trotting_wagons.common.entities.base.AbstractWagon", remap = false)
public abstract class TrottingWagonsHitchMixin {

    @Inject(method = "tryHitchHorse", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/Level;isClientSide:Z"),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, require = 0)
    private void bh_refuseCart(Player player, CallbackInfoReturnable<Boolean> cir, Mob mob) {
        if (mob instanceof AbstractHorse horse && IHorseData.of(horse).bh_hasCartGear()) {
            if (!horse.level().isClientSide()) {
                player.displayClientMessage(Component.translatable("message.icys-better-horses.wagon_cart_attached")
                        .withStyle(ChatFormatting.RED), false);
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "lambda$tryHitchHorse$0", at = @At("HEAD"), cancellable = true, require = 0)
    private static void bh_allowBreeds(Player player, Mob mob, CallbackInfoReturnable<Boolean> cir) {
        if (mob.getLeashHolder() == player && BhHorseKind.managed(mob)) {
            cir.setReturnValue(true);
        }
    }

}
