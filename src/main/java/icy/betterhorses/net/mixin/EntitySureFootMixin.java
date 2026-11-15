package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.SureFoot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntitySureFootMixin {

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3 bh_sureFoot(Vec3 move, MoverType type, Vec3 original) {
        return SureFoot.hold((Entity) (Object) this, type, move);
    }
}
