package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhHorseCombatAlert;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerHurtMixin {

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void bh_rouseHorses(ServerLevel level, DamageSource source, float amount,
                                CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!BhConfig.horseCombatEnabled()
                || !(source.getEntity() instanceof LivingEntity threat) || threat == self) {
            return;
        }
        BhHorseCombatAlert.rouse(level, self, threat);
    }
}
