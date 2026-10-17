package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PathNavigation.class)
public abstract class PathNavigationIdleGaitMixin {

    @Shadow @Final protected Mob mob;

    @ModifyVariable(method = "moveTo(Lnet/minecraft/world/level/pathfinder/Path;D)Z", at = @At("HEAD"), argsOnly = true)
    private double bh$idleGait(double speed) {
        return this.mob instanceof AbstractHorse horse && !horse.level().isClientSide()
                ? IHorseData.of(horse).bh_idleSpeedModifier(speed)
                : speed;
    }
}
