package icy.betterhorses.net.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.Redirect;
import icy.betterhorses.net.feature.breed.WinterFeet;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class DonkeyMovementMixin {

    @Redirect(method = "travel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
    private float bh_iceTraction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return WinterFeet.friction((LivingEntity) (Object) this, state.getFriction(level, pos, entity));
    }
}
