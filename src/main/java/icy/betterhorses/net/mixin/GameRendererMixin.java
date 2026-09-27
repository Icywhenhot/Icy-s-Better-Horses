package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhEquineGait;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "extract", at = @At("HEAD"))
    private void bh_newFrame(CallbackInfo ci) {
        BhEquineGait.newFrame();
    }
}
