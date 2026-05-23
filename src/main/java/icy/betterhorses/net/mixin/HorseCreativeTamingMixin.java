package icy.betterhorses.net.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Horse.class)
public abstract class HorseCreativeTamingMixin extends AbstractHorse {

    protected HorseCreativeTamingMixin(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void bh_tameWithCreativeSaddle(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.isCreative()
                || isBaby()
                || !isAlive()
                || isTamed()
                || !getItemBySlot(EquipmentSlot.SADDLE).isEmpty()
                || !(player.getItemInHand(hand).get(DataComponents.EQUIPPABLE) instanceof Equippable equippable)
                || equippable.slot() != EquipmentSlot.SADDLE) {
            return;
        }

        if (!level().isClientSide()) {
            tameWithName(player);
        }
        cir.setReturnValue(level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }
}
