package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Grants +10 bond when a player names a tamed horse with a name tag.
 *
 * Vanilla {@code Mob.interact} calls {@code checkAndHandleImportantInteractions} first; that
 * method handles name-tag renames via {@code ItemStack.interactLivingEntity} and short-circuits
 * with a CONSUME result before {@code mobInteract} runs. So a {@code mobInteract} hook never
 * sees name-tag use — we have to attach to {@code interact} on Mob itself.
 */
@Mixin(Mob.class)
public abstract class MobNameTagBondMixin {

    @Unique private boolean bh$nameTagInteractInFlight = false;
    @Unique private @Nullable Component bh$nameBeforeInteract = null;

    @Inject(method = "interact", at = @At("HEAD"))
    private void bh$captureNameTagState(Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
        Mob self = (Mob) (Object) this;
        if (!(self instanceof AbstractHorse)) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.NAME_TAG)) {
            return;
        }
        this.bh$nameTagInteractInFlight = true;
        this.bh$nameBeforeInteract = self.getCustomName();
    }

    @Inject(method = "interact", at = @At("RETURN"))
    private void bh$rewardNameTagBond(Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            if (!this.bh$nameTagInteractInFlight) {
                return;
            }
            Mob self = (Mob) (Object) this;
            if (self.level().isClientSide() || !cir.getReturnValue().consumesAction()) {
                return;
            }
            if (!(self instanceof IHorseData horseData)) {
                return;
            }
            if (horseData.bh_getOwner() == null) {
                return;
            }
            Component now = self.getCustomName();
            if (now != null && !now.equals(this.bh$nameBeforeInteract)) {
                horseData.bh_setBond(horseData.bh_getBond() + 10);
            }
        } finally {
            this.bh$nameTagInteractInFlight = false;
            this.bh$nameBeforeInteract = null;
        }
    }
}
