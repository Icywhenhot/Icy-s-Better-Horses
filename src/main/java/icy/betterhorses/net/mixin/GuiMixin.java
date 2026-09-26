package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.BhInventoryEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void bh_hideEffectsBehindHorseScreen(GuiGraphics gfx, CallbackInfo ci) {
        if (this.minecraft.screen instanceof HorseInventoryScreen screen && BhInventoryEffects.fits(screen)) {
            ci.cancel();
        }
    }
}
