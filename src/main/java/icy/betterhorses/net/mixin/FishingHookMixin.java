package icy.betterhorses.net.mixin;

import icy.betterhorses.net.feature.breed.CoastalScout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {

    @Shadow private int timeUntilLured;
    @Unique private int bh_previousWait;

    @Inject(method = "catchingFish", at = @At("HEAD"))
    private void bh_recordWait(BlockPos pos, CallbackInfo ci) {
        bh_previousWait = timeUntilLured;
    }

    @Inject(method = "catchingFish", at = @At("RETURN"))
    private void bh_coastalFishing(BlockPos pos, CallbackInfo ci) {
        if (bh_previousWait > 0 || timeUntilLured <= 0) {
            return;
        }
        Player player = ((FishingHook) (Object) this).getPlayerOwner();
        if (player != null && CoastalScout.fishingNearby(player)) {
            timeUntilLured = Math.max(1, (int) Math.ceil(timeUntilLured * 0.85D));
        }
    }
}
