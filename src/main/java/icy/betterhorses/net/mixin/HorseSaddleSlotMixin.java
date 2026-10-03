package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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

    private HorseSaddleSlotMixin() {
        super(null, 0, 0, 0);
    }

    @Override
    public boolean mayPickup(Player player) {
        if (IHorseData.of(this.val$horse).bh_hasCartGear()) {
            return false;
        }
        return super.mayPickup(player);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void bh_acceptUpgradedSaddle(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModItems.UPGRADED_SADDLE)
                && !hasItem()
                && this.val$horse.isSaddleable()) {
            cir.setReturnValue(true);
        }
    }
}
