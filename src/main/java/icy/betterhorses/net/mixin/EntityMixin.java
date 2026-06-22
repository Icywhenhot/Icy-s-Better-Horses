package icy.betterhorses.net.mixin;

import icy.betterhorses.net.IHorseData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private static final Identifier BH_MOUNTED_STEP_HEIGHT_ID =
            Identifier.fromNamespaceAndPath("icys_better_horses", "mounted_step_height");
    @Unique
    private static final Identifier BH_MOUNTED_BREAK_SPEED_ID =
            Identifier.fromNamespaceAndPath("icys_better_horses", "mounted_break_speed");
    @Unique private static final double BH_MOUNTED_STEP_HEIGHT_BONUS = 0.1D;
    @Unique private static final double BH_MOUNTED_BREAK_SPEED_BONUS = 5.0D;
    @Unique private @Nullable AbstractHorse bh_dismountHorse = null;
    @Unique private boolean bh_shouldSetHorseToWanderOnDismount = false;

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At("TAIL"))
    private void bh_applyMountedHorseBonuses(
            Entity vehicle,
            boolean force,
            boolean sendGameEvent,
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

    @Inject(
            method = "interact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;canShearEquipment(Lnet/minecraft/world/entity/player/Player;)Z"
            ),
            cancellable = true)
    private void bh_blockNonOwnerHorseSaddleShearing(
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide()
                || !(self instanceof AbstractHorse horse)
                || !bh_shouldBlockHorseSaddleShearing(horse, player, player.getItemInHand(hand))) {
            return;
        }

        horse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable("message.icys_better_horses.not_shear_owner"));
        }
        cir.setReturnValue(InteractionResult.CONSUME);
    }

    @Unique
    private static boolean bh_shouldBlockHorseSaddleShearing(AbstractHorse horse, Player player, ItemStack heldItem) {
        if (!heldItem.is(Items.SHEARS) || player.isSecondaryUseActive()) {
            return false;
        }

        IHorseData data = (IHorseData) horse;
        if (data.bh_getOwner() == null || data.bh_getOwner().equals(player.getUUID())) {
            return false;
        }

        ItemStack saddle = horse.getItemBySlot(EquipmentSlot.SADDLE);
        if (saddle.isEmpty()) {
            return false;
        }

        net.minecraft.world.item.equipment.Equippable equippable = saddle.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.canBeSheared();
    }
}
