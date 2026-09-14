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

    @org.spongepowered.asm.mixin.injection.Redirect(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V"))
    private void bh_rescueRider(Player player, float health, DamageSource source, float amount) {
        if (health <= 0.0F && player.level() instanceof ServerLevel level
                && icy.betterhorses.net.BhSecondChance.intercept(level, player, source, player.getHealth() - health)) {
            return;
        }
        player.setHealth(health);
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void bh_rouseHorses(DamageSource source, float amount, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!BhConfig.horseCombatEnabled()
                || !(self.level() instanceof ServerLevel level)
                || !(source.getEntity() instanceof LivingEntity threat) || threat == self) {
            return;
        }
        BhHorseCombatAlert.rouse(level, self, threat);
    }
}
