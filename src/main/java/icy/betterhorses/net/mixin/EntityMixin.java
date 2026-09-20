package icy.betterhorses.net.mixin;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseTracker;
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
import net.minecraftforge.common.ForgeMod;
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

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("TAIL"))
    private void bh_applyMountedHorseBonuses(
            Entity vehicle,
            boolean force,
            CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!cir.getReturnValueZ() || !(self instanceof ServerPlayer player) || !(vehicle instanceof AbstractHorse horse)) {
            return;
        }

        if (player.getUUID().equals(IHorseData.of(horse).bh_getOwner())) {
            HorseTracker.setLastRidden(player.getUUID(), horse);
        }

        @Nullable AttributeInstance stepHeight = horse.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
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
        if (!((Object) this instanceof ServerPlayer player)
                || !(player.getVehicle() instanceof AbstractHorse horse)) return;
        if (horse.getPassengers().size() == 1) {
            AttributeInstance stepHeight = horse.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
            if (stepHeight != null) stepHeight.removeModifier(BH_MOUNTED_STEP_HEIGHT_ID);
        }
        IHorseData data = IHorseData.of(horse);
        if (!player.getUUID().equals(data.bh_getOwner())) return;
        HorseTracker.setLastRidden(player.getUUID(), horse);
        data.bh_setWanderCenter(horse.blockPosition());
        data.bh_setCommand(HorseCommand.WANDER);
    }

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void bh_cartRidersDoNotSuffocate(CallbackInfoReturnable<Boolean> cir) {
        if (((Entity) (Object) this).getVehicle() instanceof HorseCartEntity) {
            cir.setReturnValue(false);
        }
    }
}
