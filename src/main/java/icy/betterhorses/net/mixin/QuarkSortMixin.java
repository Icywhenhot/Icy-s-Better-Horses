package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.base.handler.SortingHandler", remap = false)
public abstract class QuarkSortMixin {

    @Shadow
    public static void sortInventory(Container container, int from, int to, int[] locked) {
    }

    @Inject(method = "sortInventory(Lnet/minecraft/world/entity/player/Player;Z)V",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Player;containerMenu:Lnet/minecraft/world/inventory/AbstractContainerMenu;",
                    opcode = Opcodes.GETFIELD, ordinal = 0),
            cancellable = true, require = 0)
    private static void bh_sortHorseStorage(Player player, boolean forcePlayer, CallbackInfo ci) {
        if (forcePlayer || !(player.containerMenu instanceof HorseInventoryLayoutAccess access)) {
            return;
        }
        for (HorseInventoryLayoutAccess.Storage s : access.bh_storage()) {
            sortInventory(s.container(), s.from(), s.to(), null);
        }
        ci.cancel();
    }
}
