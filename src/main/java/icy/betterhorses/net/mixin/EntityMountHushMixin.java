package icy.betterhorses.net.mixin;

import org.spongepowered.asm.mixin.injection.Redirect;
import icy.betterhorses.net.feature.breed.SculkHush;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMountHushMixin {

    @Redirect(method = {"addPassenger", "removePassenger"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;)V"))
    private void bh_quietSaddle(Entity self, GameEvent event, Entity passenger) {
        if (!SculkHush.hushed(self)) self.gameEvent(event, passenger);
    }
}
