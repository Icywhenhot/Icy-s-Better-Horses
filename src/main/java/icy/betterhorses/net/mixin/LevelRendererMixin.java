package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhHorseRenderState;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void bh_enterLevel(CallbackInfo ci) {
        BhHorseRenderState.enterLevel();
    }

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void bh_leaveLevel(CallbackInfo ci) {
        BhHorseRenderState.leaveLevel();
    }
}
