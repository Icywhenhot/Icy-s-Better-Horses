package icy.betterhorses.net.client.mixin;

import icy.betterhorses.net.client.render.BhMountedHorseVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Tilts the head down/back when the rider is in first-person, so it doesn't block the view.
@Mixin(HorseModel.class)
public abstract class HorseModelMixin<T extends AbstractHorse> extends AgeableListModel<T> {

    @Shadow @Final protected ModelPart headParts;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/animal/horse/AbstractHorse;FFFFF)V", at = @At("RETURN"))
    private void bh_lowerHeadWhenRidden(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                        float netHeadYaw, float headPitch, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null
                && mc.options.getCameraType().isFirstPerson()
                && entity.hasPassenger(mc.player)) {
            this.headParts.xRot = Math.min(this.headParts.xRot + BhMountedHorseVisibility.HEAD_PITCH_OFFSET, 1.5F);
            this.headParts.y += BhMountedHorseVisibility.HEAD_Y_OFFSET;
        }
    }
}
