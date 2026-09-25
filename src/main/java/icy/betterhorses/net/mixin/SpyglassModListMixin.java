package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IcysBetterHorses;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentMap;

@Pseudo
@Mixin(targets = "com.brokenkeyboard.usefulspyglass.DrawOverlay", remap = false)
public abstract class SpyglassModListMixin {

    @Shadow @Final private static ConcurrentMap<String, String> MODLIST;

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 0)
    private static void bh_aliasNamespace(CallbackInfo ci) {
        String name = MODLIST.get(IcysBetterHorses.MOD_ID);
        if (name != null) {
            MODLIST.putIfAbsent(IcysBetterHorses.RESOURCE_NAMESPACE, name);
        }
    }
}
