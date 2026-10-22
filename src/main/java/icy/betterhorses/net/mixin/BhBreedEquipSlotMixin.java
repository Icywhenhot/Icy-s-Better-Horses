package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhArmorSlotAccess;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
        "net/minecraft/world/inventory/HorseInventoryMenu$1",
        "net/minecraft/world/inventory/HorseInventoryMenu$2"
})
public abstract class BhBreedEquipSlotMixin {

    @Inject(method = "isActive", at = @At("RETURN"), cancellable = true)
    private void bh_keepBreedSlotsActive(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !(this instanceof BhArmorSlotAccess access)) {
            return;
        }
        if (!(access.bh_slotOwner() instanceof BhBreedHorse horse)) {
            return;
        }
        EquipmentSlot slot = access.bh_equipmentSlot();
        if (slot != EquipmentSlot.SADDLE && slot != EquipmentSlot.BODY) {
            return;
        }
        if (horse.canUseSlot(slot)) {
            cir.setReturnValue(true);
        }
    }
}
