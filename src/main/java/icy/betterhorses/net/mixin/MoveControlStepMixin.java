package icy.betterhorses.net.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MoveControl.class)
public abstract class MoveControlStepMixin {

    @Shadow @Final protected Mob mob;
    @Shadow protected double wantedX;
    @Shadow protected double wantedZ;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/control/JumpControl;jump()V"))
    private void bh_walkUpInsteadOfJumping(JumpControl control) {
        if (!(this.mob instanceof AbstractHorse horse) || !bh_canStepOver(horse)) {
            control.jump();
        }
    }

    @Unique
    private boolean bh_canStepOver(AbstractHorse horse) {
        float step = horse.maxUpStep();
        if (step <= 0.0F || !horse.onGround()) {
            return false;
        }
        Vec3 aim = new Vec3(this.wantedX - horse.getX(), 0.0D, this.wantedZ - horse.getZ());
        if (aim.horizontalDistanceSqr() < 1.0E-6D) {
            return false;
        }
        AABB box = horse.getBoundingBox();
        if (!horse.level().noCollision(horse, box.deflate(1.0E-7D))) {
            return false;
        }
        AABB ahead = box.move(aim.normalize().scale(0.4D));
        double ceiling = horse.getY() + step + 1.0E-7D;
        for (VoxelShape shape : horse.level().getBlockCollisions(horse, ahead)) {
            if (shape.bounds().maxY > ceiling) {
                return false;
            }
        }
        return true;
    }
}
