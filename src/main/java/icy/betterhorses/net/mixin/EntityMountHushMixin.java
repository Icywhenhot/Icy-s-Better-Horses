package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import icy.betterhorses.net.feature.breed.SculkHush;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMountHushMixin {

    @WrapOperation(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;)V"))
    private void bh_quietMount(Level level, Entity rider, Holder<GameEvent> event, Vec3 pos, Operation<Void> original) {
        if (!SculkHush.hushed(rider.getVehicle())) original.call(level, rider, event, pos);
    }

    @WrapOperation(method = "removeVehicle()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;)V"))
    private void bh_quietDismount(Level level, Entity rider, Holder<GameEvent> event, Vec3 pos,
                                  Operation<Void> original, @Local(index = 1) Entity vehicle) {
        if (!SculkHush.hushed(vehicle)) original.call(level, rider, event, pos);
    }
}
