package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    private static final ResourceLocation BH_MOUNTED_STEP_HEIGHT_ID =
            new ResourceLocation("icys_better_horses", "mounted_step_height");
    @Unique
    private static final ResourceLocation BH_MOUNTED_BREAK_SPEED_ID =
            new ResourceLocation("icys_better_horses", "mounted_break_speed");
    @Unique private static final double BH_MOUNTED_STEP_HEIGHT_BONUS = 0.1D;
    @Unique private static final double BH_MOUNTED_BREAK_SPEED_BONUS = 5.0D;
    @Unique private @Nullable AbstractHorse bh_dismountHorse = null;
    @Unique private boolean bh_shouldSetHorseToWanderOnDismount = false;

    /**
     * 1.21.1 Entity.startRiding(Entity, boolean) — 2-arg form (the 3-arg sendGameEvent variant is 1.21.5+).
     */
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("TAIL"))
    private void bh_applyMountedHorseBonuses(
            Entity vehicle,
            boolean force,
            CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!cir.getReturnValueZ() || !(self instanceof ServerPlayer player) || !(vehicle instanceof AbstractHorse horse)) {
            return;
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

        this.bh_dismountHorse = null;
        this.bh_shouldSetHorseToWanderOnDismount = false;
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AbstractHorse horse) {
            if (horse.getPassengers().size() == 1) {
                @Nullable AttributeInstance stepHeight = horse.getAttribute(Attributes.STEP_HEIGHT);
                if (stepHeight != null) {
                    stepHeight.removeModifier(BH_MOUNTED_STEP_HEIGHT_ID);
                }
            }
            this.bh_dismountHorse = horse;
            this.bh_shouldSetHorseToWanderOnDismount = player.getUUID().equals(((IHorseData) horse).bh_getOwner());
        }

        @Nullable AttributeInstance breakSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (breakSpeed != null) {
            breakSpeed.removeModifier(BH_MOUNTED_BREAK_SPEED_ID);
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

        ((IHorseData) horse).bh_setWanderCommand(player.blockPosition());
    }
}
