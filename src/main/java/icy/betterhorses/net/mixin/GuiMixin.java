package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import icy.betterhorses.net.client.BhInventoryEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique private int bh_horseHeartRows;

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int bh_keepHungerOnHorseback(Gui gui, LivingEntity vehicle, Operation<Integer> original) {
        int hearts = original.call(gui, vehicle);
        this.bh_horseHeartRows = vehicle instanceof AbstractHorse && hearts > 0 ? (hearts + 9) / 10 : 0;
        return this.bh_horseHeartRows > 0 ? 0 : hearts;
    }

    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;getVisibleVehicleHeartRows(I)I"))
    private int bh_airAboveHorseHearts(Gui gui, int hearts, Operation<Integer> original) {
        return this.bh_horseHeartRows > 0 ? this.bh_horseHeartRows : original.call(gui, hearts);
    }

    @ModifyConstant(method = "renderVehicleHealth", constant = @Constant(intValue = 39))
    private int bh_horseHeartsAboveHunger(int bottom) {
        return this.minecraft.gameMode.canHurtPlayer() && this.minecraft.player.getVehicle() instanceof AbstractHorse
                ? bottom + 10
                : bottom;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
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

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void bh_hideEffectsBehindHorseScreen(GuiGraphics gfx, CallbackInfo ci) {
        if (this.minecraft.screen instanceof HorseInventoryScreen screen && BhInventoryEffects.fits(screen)) {
            ci.cancel();
        }
    }
}
