package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// makes riding in the cart's bed as safe as riding in a boat
@Mixin(Mob.class)
public abstract class MobCartPassengerMixin {

    @Shadow @Final protected GoalSelector goalSelector;

    // true while this mob is being carried by a cart, in the bed, or riding shotgun on the bench
    @Unique
    private boolean bh_isCartCargo() {
        Entity vehicle = ((Mob) (Object) this).getVehicle();
        if (vehicle instanceof HorseCartEntity) {
            return true;
        }
        // bench cargo is a passenger of the horse itself, not of the cart behind
        return vehicle instanceof AbstractHorse horse && ((IHorseData) horse).bh_hasCartGear();
    }

    // sneak click a carried mob to set it down. the cart's own sneak click can't reach the one riding
    // shotgun, since clicking it hits the mob's hitbox and never the cart's
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void bh_setDownOnSneakClick(
            Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.isSecondaryUseActive() || !this.bh_isCartCargo()) {
            return;
        }

        Mob self = (Mob) (Object) this;
        if (!self.level().isClientSide()) {
            HorseCartEntity cart = this.bh_carryingCart();
            if (cart != null) {
                // steps it clear of the bed too, so the auto boarder doesn't just pick it back up
                cart.setDown(self);
            } else {
                self.stopRiding();
            }
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    // the cart doing the carrying, whether this mob rides the cart itself or the horse pulling it
    @Unique
    private @Nullable HorseCartEntity bh_carryingCart() {
        Entity vehicle = ((Mob) (Object) this).getVehicle();
        if (vehicle instanceof HorseCartEntity cart) {
            return cart;
        }
        return vehicle instanceof AbstractHorse horse ? ((IHorseData) horse).bh_getCartEntity() : null;
    }

    // mirrors what vanilla does for boats: a carried mob stops trying to jump
    @Inject(method = "updateControlFlags", at = @At("TAIL"))
    private void bh_dropJumpFlagInCart(CallbackInfo ci) {
        if (this.bh_isCartCargo()) {
            this.goalSelector.setControlFlag(Goal.Flag.JUMP, false);
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void bh_noNewTargetsInCart(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (this.bh_isCartCargo()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getTarget", at = @At("HEAD"), cancellable = true)
    private void bh_forgetTargetInCart(CallbackInfoReturnable<LivingEntity> cir) {
        if (this.bh_isCartCargo()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void bh_landNoHitsInCart(ServerLevel level, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (this.bh_isCartCargo()) {
            cir.setReturnValue(false);
        }
    }
}
