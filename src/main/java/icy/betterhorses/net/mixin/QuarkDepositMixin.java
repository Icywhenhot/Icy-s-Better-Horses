package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.base.handler.InventoryTransferHandler$Transfer", remap = false)
public abstract class QuarkDepositMixin {

    @Shadow @Final public Player player;
    @Shadow @Final public List<Pair<IItemHandler, Double>> itemHandlers;

    @Inject(method = "locateItemHandlers", at = @At("HEAD"), cancellable = true, require = 0)
    private void bh_horseStorage(CallbackInfo ci) {
        if (!(this.player.containerMenu instanceof HorseInventoryLayoutAccess access)) {
            return;
        }
        for (HorseInventoryLayoutAccess.Storage s : access.bh_storage()) {
            this.itemHandlers.add(Pair.of(new RangedWrapper(new InvWrapper(s.container()), s.from(), s.to()), 0.0D));
        }
        ci.cancel();
    }
}
