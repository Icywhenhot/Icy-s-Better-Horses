package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.feature.breed.Ironclad;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "setRemoved", at = @At("HEAD"))
    private void bh_removeEffects(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!self.isRemoved() && self instanceof AbstractHorse horse) IHorseData.of(horse).bh_onRemoved();
    }

    @Unique
    private static final ResourceLocation BH_MOUNTED_STEP_HEIGHT_ID =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "mounted_step_height");
    @Unique
    private static final ResourceLocation BH_MOUNTED_BREAK_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "mounted_break_speed");
    @Unique private static final double BH_MOUNTED_STEP_HEIGHT_BONUS = 0.1D;
    @Unique private static final double BH_MOUNTED_BREAK_SPEED_BONUS = 5.0D;

    @Inject(method = "canCollideWith", at = @At("HEAD"), cancellable = true)
    private void bh_ignoreOwnCart(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof HorseCartEntity cart && !cart.bh_collidesWith((Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "deflection", at = @At("HEAD"), cancellable = true)
    private void bh_ridersDeflectProjectiles(Projectile projectile, CallbackInfoReturnable<ProjectileDeflection> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player)
                || !(self.getVehicle() instanceof AbstractHorse mount)
                || !Ironclad.deflectsProjectiles(IHorseData.of(mount))) {
            return;
        }
        if (!mount.level().isClientSide()) {
            BhSurge.pulse(IHorseData.of(mount), 0, 1);
        }
        cir.setReturnValue(ProjectileDeflection.REVERSE);
    }

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void bh_cartRidersDoNotSuffocate(CallbackInfoReturnable<Boolean> cir) {
        if (((Entity) (Object) this).getVehicle() instanceof HorseCartEntity) {
            cir.setReturnValue(false);
        }
    }

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

        @Nullable AttributeInstance stepHeight = horse.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null && stepHeight.getModifier(BH_MOUNTED_STEP_HEIGHT_ID) == null) {
            stepHeight.addTransientModifier(new AttributeModifier(
                    BH_MOUNTED_STEP_HEIGHT_ID,
                    BH_MOUNTED_STEP_HEIGHT_BONUS,
                    AttributeModifier.Operation.ADD_VALUE));
        }

        @Nullable AttributeInstance breakSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (breakSpeed != null) {
            breakSpeed.removeModifier(BH_MOUNTED_BREAK_SPEED_ID);
            breakSpeed.addTransientModifier(new AttributeModifier(
                    BH_MOUNTED_BREAK_SPEED_ID,
                    BH_MOUNTED_BREAK_SPEED_BONUS,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void bh_removeMountedHorseBonuses(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AbstractHorse horse && horse.getPassengers().size() == 1) {
            @Nullable AttributeInstance stepHeight = horse.getAttribute(Attributes.STEP_HEIGHT);
            if (stepHeight != null) {
                stepHeight.removeModifier(BH_MOUNTED_STEP_HEIGHT_ID);
            }
        }

        @Nullable AttributeInstance breakSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (breakSpeed != null) {
            breakSpeed.removeModifier(BH_MOUNTED_BREAK_SPEED_ID);
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void bh_wanderAfterDismount(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof ServerPlayer player) || !(player.getVehicle() instanceof AbstractHorse horse)) {
            return;
        }

        IHorseData data = IHorseData.of(horse);
        if (!player.getUUID().equals(data.bh_getOwner())) {
            return;
        }

        HorseTracker.setLastRidden(player.getUUID(), horse);
        data.bh_setWanderCenter(horse.blockPosition());
        data.bh_setCommand(HorseCommand.WANDER);
    }

}
