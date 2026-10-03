package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.util.HitScan", remap = false)
public abstract class PointBlankHitScanMixin {

    @ModifyExpressionValue(method = {"getNearestObjectInCrosshair*", "ensureEntityInCrosshair*"},
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"),
            require = 0)
    private static List<Entity> bh_skipOwnMount(List<Entity> hits, @Local(argsOnly = true) LivingEntity shooter) {
        if (shooter.getVehicle() == null) return hits;
        return hits.stream().filter(e -> !shooter.isPassengerOfSameVehicle(e)).toList();
    }
}
