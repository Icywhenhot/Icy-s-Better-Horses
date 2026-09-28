package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.base.handler.InventoryTransferHandler", remap = false)
public abstract class QuarkButtonsMixin {

    @Inject(method = "accepts", at = @At("HEAD"), cancellable = true, require = 0)
    private static void bh_onlyWithStorage(AbstractContainerMenu menu, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (menu instanceof HorseInventoryLayoutAccess access) {
            cir.setReturnValue(!access.bh_storage().isEmpty());
        }
    }
}
