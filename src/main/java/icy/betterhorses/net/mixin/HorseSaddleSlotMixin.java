package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.HorseInventoryMenu$1")
public abstract class HorseSaddleSlotMixin extends Slot {

    @Shadow @Final AbstractHorse val$horse;

    private HorseSaddleSlotMixin(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void bh_allowUpgradedSaddle(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && stack.is(ModItems.UPGRADED_SADDLE)) {
            cir.setReturnValue(true);
        }
    }

    // Server-side twin of the screen lock; extends Slot so the override gets remapped in the jar.
    @Override
    public boolean mayPickup(Player player) {
        return !IHorseData.of(val$horse).bh_hasCartGear() && super.mayPickup(player);
    }
}
