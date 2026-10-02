package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Gui.class)
public abstract class HudMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique private int bh_horseHeartRows;

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int bh_keepHungerOnHorseback(Gui hud, LivingEntity vehicle, Operation<Integer> original) {
        int hearts = original.call(hud, vehicle);
        this.bh_horseHeartRows = vehicle instanceof AbstractHorse && hearts > 0 ? (hearts + 9) / 10 : 0;
        return this.bh_horseHeartRows > 0 ? 0 : hearts;
    }

    @WrapOperation(method = "getAirBubbleYLine", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;getVisibleVehicleHeartRows(I)I"))
    private int bh_airAboveHorseHearts(Gui hud, int hearts, Operation<Integer> original) {
        return this.bh_horseHeartRows > 0 ? this.bh_horseHeartRows : original.call(hud, hearts);
    }

    @ModifyConstant(method = "renderVehicleHealth", constant = @Constant(intValue = 39))
    private int bh_horseHeartsAboveHunger(int bottom) {
        return this.minecraft.gameMode.canHurtPlayer() && this.minecraft.player.getVehicle() instanceof AbstractHorse
                ? bottom + 10
                : bottom;
    }

    @WrapOperation(method = "nextContextualInfoState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private PlayerRideableJumping bh_xpBarWhileRiding(LocalPlayer player, Operation<PlayerRideableJumping> original) {
        PlayerRideableJumping vehicle = original.call(player);
        return vehicle instanceof AbstractHorse
                && this.minecraft.gameMode.hasExperience()
                && !this.minecraft.options.keyJump.isDown()
                && player.getJumpRidingScale() <= 0.0F
                ? null
                : vehicle;
    }
}
