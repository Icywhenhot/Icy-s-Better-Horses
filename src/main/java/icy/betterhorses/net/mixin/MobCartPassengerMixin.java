package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes riding in the cart's bed as safe as riding in a boat: a mob being carried can't reach the
 * people carrying it.
 *
 * <p>Vanilla's own boat handling is only the {@link Goal.Flag#JUMP} line in
 * {@code Mob.updateControlFlags} — a passenger in a real boat can still swing at you, and a creeper
 * in one still goes off. The cart is meant to be a way to <i>haul</i> animals and villagers around,
 * so it goes further: while a mob is in the bed it has no attack target and lands no hits.</p>
 *
 * <p>Three overlapping guards rather than one, because mobs reach violence by different routes:
 * {@code canAttack} stops a target being picked up in the first place, {@code getTarget} neutralises
 * one the mob boarded with (this is also what defuses a creeper — {@code SwellGoal} keys off the
 * target, so it stands down and the fuse winds back), and {@code doHurtTarget} is the backstop for
 * anything that swings without going through either.</p>
 */
@Mixin(Mob.class)
public abstract class MobCartPassengerMixin {

    @Shadow @Final protected GoalSelector goalSelector;

    /** True while this mob is being carried by a cart — in the bed, or riding shotgun on the bench. */
    @Unique
    private boolean bh_isCartCargo() {
        Entity vehicle = ((Mob) (Object) this).getVehicle();
        if (vehicle instanceof HorseCartEntity) {
            return true;
        }
        // Bench cargo is a passenger of the horse itself, not of the cart behind it — without this
        // a creeper sat next to the driver would still go off in their face.
        return vehicle instanceof AbstractHorse horse && ((IHorseData) horse).bh_hasCartGear();
    }

    /**
     * Mirrors what vanilla does for boats: a carried mob stops trying to jump. Vanilla's own line
     * only covers {@code AbstractBoat}, so the cart has to add itself here.
     */
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
