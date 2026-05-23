package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import icy.betterhorses.net.client.render.IBhEquineStabilizerState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractEquineModel.class)
public abstract class AbstractEquineModelMixin<T extends EquineRenderState> extends EntityModel<T> {

    @Shadow @Final protected ModelPart headParts;

    protected AbstractEquineModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EquineRenderState;)V", at = @At("RETURN"))
    private void bh_lowerHeadWhenRidden(T state, CallbackInfo ci) {
        if (!(state instanceof IBhEquineStabilizerState bhState) || !bhState.bh_isRiddenByPlayerInFirstPerson()) {
            return;
        }

        this.headParts.xRot = Math.min(this.headParts.xRot + BhMountedHorseVisibility.HEAD_PITCH_OFFSET, 1.5F);
        this.headParts.y += BhMountedHorseVisibility.HEAD_Y_OFFSET;
    }
}
