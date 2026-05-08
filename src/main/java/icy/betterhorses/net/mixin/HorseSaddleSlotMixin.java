package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
 * 1.21.11 moved horse saddle acceptance into the shared package-private {@code ArmorSlot}.
 * Allow the upgraded saddle when that slot represents an {@link EquipmentSlot#SADDLE} on a horse.
 */
@Mixin(targets = "net/minecraft/world/inventory/ArmorSlot")
public abstract class HorseSaddleSlotMixin extends Slot {

    @Shadow @Final
    private LivingEntity owner;

    @Shadow @Final
    private EquipmentSlot slot;

    // Never used; satisfies compiler so `this` exposes Slot members.
    private HorseSaddleSlotMixin() {
        super(null, 0, 0, 0);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void bh_acceptUpgradedSaddle(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.slot == EquipmentSlot.SADDLE
                && this.owner instanceof AbstractHorse horse
                && stack.is(ModItems.UPGRADED_SADDLE)
                && !hasItem()
                && horse.canUseSlot(EquipmentSlot.SADDLE)) {
            cir.setReturnValue(true);
        }
    }
}
