package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import icy.betterhorses.net.feature.breed.SculkHush;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMountHushMixin {

    @WrapWithCondition(method = {"addPassenger", "removePassenger"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V"))
    private boolean bh_quietSaddle(Entity self, Holder<GameEvent> event, Entity passenger) {
        return !SculkHush.hushed(self);
    }
}
