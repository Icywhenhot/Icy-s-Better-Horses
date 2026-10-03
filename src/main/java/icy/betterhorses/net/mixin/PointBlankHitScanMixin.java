package icy.betterhorses.net.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Pseudo
@Mixin(targets = "com.vicmatskiv.pointblank.util.HitScan", remap = false)
public abstract class PointBlankHitScanMixin {

    @Redirect(method = {"getNearestObjectInCrosshair*", "ensureEntityInCrosshair*"},
            at = @At(value = "INVOKE", remap = true,
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"),
            require = 0)
    private static List<Entity> bh_skipOwnMount(Level level, Entity shooter, AABB box) {
        List<Entity> hits = level.getEntities(shooter, box);
        if (shooter == null || shooter.getVehicle() == null) return hits;
        return hits.stream().filter(e -> !shooter.isPassengerOfSameVehicle(e)).toList();
    }
}
