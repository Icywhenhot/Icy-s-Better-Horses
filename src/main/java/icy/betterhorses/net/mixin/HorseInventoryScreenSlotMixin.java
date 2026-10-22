package icy.betterhorses.net.mixin;

import icy.betterhorses.net.entity.BhBreedHorse;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenSlotMixin {

    @Inject(method = "shouldRenderSaddleSlot", at = @At("RETURN"), cancellable = true)
    private void bh_drawBreedSaddleSlot(CallbackInfoReturnable<Boolean> cir) {
        this.bh_keepSlot(cir, EquipmentSlot.SADDLE);
    }

    @Inject(method = "shouldRenderArmorSlot", at = @At("RETURN"), cancellable = true)
    private void bh_drawBreedArmorSlot(CallbackInfoReturnable<Boolean> cir) {
        this.bh_keepSlot(cir, EquipmentSlot.BODY);
    }

    @Unique
    private void bh_keepSlot(CallbackInfoReturnable<Boolean> cir, EquipmentSlot slot) {
        if (cir.getReturnValueZ() || !(((AbstractMountInventoryScreenAccessor) this).bh_mount() instanceof BhBreedHorse horse)) {
            return;
        }
        if (horse.canUseSlot(slot)) {
            cir.setReturnValue(true);
        }
    }
}
