package icy.betterhorses.net.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(KeyMapping.class)
public abstract class KeyMappingShareMixin {

    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow private InputConstants.Key key;
    @Shadow private int clickCount;

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void bh$clickEveryBinding(InputConstants.Key pressed, CallbackInfo ci) {
        for (KeyMapping mapping : ALL.values()) {
            KeyMappingShareMixin shared = (KeyMappingShareMixin) (Object) mapping;
            if (shared.key.equals(pressed)) {
                shared.clickCount++;
            }
        }
        ci.cancel();
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void bh$setEveryBinding(InputConstants.Key pressed, boolean down, CallbackInfo ci) {
        for (KeyMapping mapping : ALL.values()) {
            if (((KeyMappingShareMixin) (Object) mapping).key.equals(pressed)) {
                mapping.setDown(down);
            }
        }
        ci.cancel();
    }
}
