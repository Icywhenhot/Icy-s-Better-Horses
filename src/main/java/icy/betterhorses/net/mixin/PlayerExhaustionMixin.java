package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.PegaCompanion;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {

    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float bh_easySeat(float amount) {
        return PegaCompanion.exhaustion((Player) (Object) this, amount);
    }
}
