package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/world/inventory/ArmorSlot")
public abstract class HorseSaddleSlotMixin extends Slot {

    @Shadow @Final
    private LivingEntity owner;

    @Shadow @Final
    private EquipmentSlot slot;

    private HorseSaddleSlotMixin() {
        super(null, 0, 0, 0);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void bh_holdSaddleWhileCartHitched(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.slot == EquipmentSlot.SADDLE
                && this.owner instanceof AbstractHorse horse
                && IHorseData.of(horse).bh_hasCartGear()) {
            cir.setReturnValue(false);
        }
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
