package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Grants a one-time +10 bond the first time a player name-tags a horse.
@Mixin(NameTagItem.class)
public class NameTagItemMixin {

    @Inject(method = "interactLivingEntity", at = @At("RETURN"))
    private void bh_grantNameTagBond(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (target.level().isClientSide()) return;
        if (!cir.getReturnValue().consumesAction()) return;
        if (!(target instanceof AbstractHorse horse)) return;

        IHorseData data = (IHorseData) horse;
        if (data.bh_hasReceivedNameTagBond()) return;

        data.bh_setBond(data.bh_getBond() + 10);
        data.bh_setReceivedNameTagBond(true);
    }
}
