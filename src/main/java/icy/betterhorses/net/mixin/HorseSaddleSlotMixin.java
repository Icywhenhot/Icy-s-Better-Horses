package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The anonymous saddle slot inside {@link net.minecraft.world.inventory.HorseInventoryMenu}
 * checks {@code stack.is(Items.SADDLE)}. Extend acceptance to also allow our upgraded saddle.
 */
@Mixin(targets = "net/minecraft/world/inventory/HorseInventoryMenu$1")
public abstract class HorseSaddleSlotMixin extends Slot {

    @Shadow @Final
    AbstractHorse val$horse;

    // Never used; satisfies compiler so `this` exposes Slot members.
    private HorseSaddleSlotMixin() {
        super(null, 0, 0, 0);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void bh_acceptUpgradedSaddle(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModItems.UPGRADED_SADDLE) && !hasItem() && val$horse.canUseSlot(EquipmentSlot.SADDLE)) {
            cir.setReturnValue(true);
        }
    }
}
