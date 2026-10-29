package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseInventoryLayoutAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.base.handler.SortingHandler", remap = false)
public abstract class QuarkSortMixin {

    @Unique
    private static MethodHandle bh_sorter;

    @Unique
    private static boolean bh_looked;

    @Inject(method = {"sortInventory(Lnet/minecraft/world/entity/player/Player;Z)V",
            "sortContainer(Lnet/minecraft/world/entity/player/Player;Z)V"},
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void bh_sortHorseStorage(Player player, boolean forcePlayer, CallbackInfo ci) {
        if (forcePlayer || !(player.containerMenu instanceof HorseInventoryLayoutAccess access)) {
            return;
        }
        MethodHandle sorter = bh_sorter();
        if (sorter == null) {
            return;
        }
        try {
            for (HorseInventoryLayoutAccess.Storage s : access.bh_storage()) {
                sorter.invoke((IItemHandler) new InvWrapper(s.container()), s.from(), s.to(), (int[]) null);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        ci.cancel();
    }

    @Unique
    private static MethodHandle bh_sorter() {
        if (!bh_looked) {
            bh_looked = true;
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType type = MethodType.methodType(void.class, IItemHandler.class, int.class, int.class, int[].class);
            for (String name : new String[]{"sortInventory", "sortContainer"}) {
                try {
                    bh_sorter = lookup.findStatic(lookup.lookupClass(), name, type);
                    break;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        return bh_sorter;
    }
}
