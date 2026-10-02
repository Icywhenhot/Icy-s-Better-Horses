package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhEquineGait;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelExtractorMixin {

    @Inject(method = "extractVisibleEntities", at = @At("HEAD"))
    private void bh_enterLevel(CallbackInfo ci) {
        BhEquineGait.enterLevel();
    }

    @Inject(method = "extractVisibleEntities", at = @At("RETURN"))
    private void bh_leaveLevel(CallbackInfo ci) {
        BhEquineGait.leaveLevel();
    }
}
