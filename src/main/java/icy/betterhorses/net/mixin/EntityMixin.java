package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhAttributes;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.feature.breed.SlowBlockImmunity;
import icy.betterhorses.net.IHorseData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void bh_shrugOffSlowBlocks(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if ((Entity) (Object) this instanceof LivingEntity living
                && SlowBlockImmunity.shrugsOffSlowBlockDamage(living, source)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canCollideWith", at = @At("HEAD"), cancellable = true)
    private void bh_ignoreOwnCart(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof HorseCartEntity cart && !cart.bh_collidesWith((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void bh_removeEffects(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.isRemoved() && self instanceof AbstractHorse horse) IHorseData.of(horse).bh_onRemoved();
    }

    @Unique
    private static final UUID BH_MOUNTED_STEP_HEIGHT_ID =
            UUID.fromString("4d2b1f3a-7c9e-4a51-8b6f-1c2d3e4f5a6b");
    @Unique private static final double BH_MOUNTED_STEP_HEIGHT_BONUS = 0.1D;
    @Unique private @Nullable AbstractHorse bh_dismountHorse = null;
    @Unique private boolean bh_shouldSetHorseToWanderOnDismount = false;

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("TAIL"))
    private void bh_applyMountedHorseBonuses(
            Entity vehicle,
            boolean force,
            CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!cir.getReturnValueZ() || !(self instanceof ServerPlayer player) || !(vehicle instanceof AbstractHorse horse)) {
            return;
        }

        @Nullable AttributeInstance stepHeight = horse.getAttribute(BhAttributes.STEP_HEIGHT_ADDITION);
        if (stepHeight != null && stepHeight.getModifier(BH_MOUNTED_STEP_HEIGHT_ID) == null) {
            stepHeight.addTransientModifier(new AttributeModifier(
                    BH_MOUNTED_STEP_HEIGHT_ID,
                    "bh_mounted_step_height",
                    BH_MOUNTED_STEP_HEIGHT_BONUS,
                    AttributeModifier.Operation.ADDITION));
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void bh_removeMountedHorseBonuses(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        this.bh_dismountHorse = null;
        this.bh_shouldSetHorseToWanderOnDismount = false;
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AbstractHorse horse) {
            if (horse.getPassengers().size() == 1) {
                @Nullable AttributeInstance stepHeight = horse.getAttribute(BhAttributes.STEP_HEIGHT_ADDITION);
                if (stepHeight != null) {
                    stepHeight.removeModifier(BH_MOUNTED_STEP_HEIGHT_ID);
                }
            }
            this.bh_dismountHorse = horse;
            this.bh_shouldSetHorseToWanderOnDismount = player.getUUID().equals(((IHorseData) horse).bh_getOwner());
        }
    }

    @Inject(method = "removeVehicle", at = @At("TAIL"))
    private void bh_setHorseToWanderAfterOwnerDismount(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        AbstractHorse horse = this.bh_dismountHorse;
        boolean shouldSetWander = this.bh_shouldSetHorseToWanderOnDismount;
        this.bh_dismountHorse = null;
        this.bh_shouldSetHorseToWanderOnDismount = false;
        if (!shouldSetWander || horse == null || horse.level().isClientSide()) {
            return;
        }

        IHorseData data = (IHorseData) horse;
        data.bh_setWanderCenter(player.blockPosition());
        data.bh_setCommand(HorseCommand.WANDER);
    }
}
