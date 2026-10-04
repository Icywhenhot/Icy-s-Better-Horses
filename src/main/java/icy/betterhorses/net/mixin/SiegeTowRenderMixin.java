package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import icy.betterhorses.net.BhSiegeTow;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(GeoEntityRenderer.class)
public abstract class SiegeTowRenderMixin {

    @Inject(method = "applyRotations(Lnet/minecraft/world/entity/Entity;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V",
            at = @At("TAIL"), require = 0)
    private void bh_turnTowedSiege(Entity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw,
                                   float partialTick, float nativeScale, CallbackInfo ci) {
        if (!BhSiegeTow.towing(animatable) || BhSiegeTow.turnsItself(getClass())) return;
        poseStack.translate(0.0F, 0.0F, 3.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }
}
