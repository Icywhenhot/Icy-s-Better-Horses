package icy.betterhorses.net.mixin;

import icy.betterhorses.net.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        ItemStack held = player.getItemInHand(hand);
        boolean isSaddle = held.is(Items.SADDLE) || held.is(ModItems.UPGRADED_SADDLE.get());
        if (!player.isCreative() || isBaby() || !isAlive() || isTamed() || isSaddled() || !isSaddle) {
            return;
        }

        if (!level().isClientSide()) {
            tameWithName(player);
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level().isClientSide()));
    }
}
