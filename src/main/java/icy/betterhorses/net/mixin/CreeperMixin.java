package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseCombatAlert;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Inject(method = "explodeCreeper", at = @At("HEAD"))
    private void bh_startleHorses(CallbackInfo ci) {
        Creeper self = (Creeper) (Object) this;
        if (self.level() instanceof ServerLevel level) {
            BhHorseCombatAlert.startle(level, self.position());
        }
    }
}
