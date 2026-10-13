package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.HorseTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "net.silvertide.kindred.bond.BondService", remap = false)
public abstract class KindredSummonMixin {

    @ModifyArg(method = "materializeFresh", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static Entity bh_adoptSummonedHorse(Entity entity) {
        if (entity instanceof AbstractHorse horse && BhHorseKind.managed(horse)) {
            HorseTracker.adoptRevived(horse);
        }
        return entity;
    }
}
