package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.render.BhEquineGait;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

    @Inject(method = "extract", at = @At("HEAD"))
    private void bh_enterLevel(CallbackInfo ci) {
        BhEquineGait.enterLevel();
    }

    @Inject(method = "extract", at = @At("RETURN"))
    private void bh_leaveLevel(CallbackInfo ci) {
        BhEquineGait.leaveLevel();
    }
}
