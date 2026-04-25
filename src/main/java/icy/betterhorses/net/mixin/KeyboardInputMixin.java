package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.HorseAutodriveController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_applyHorseAutodrive(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        Screen screen = client.screen;

        boolean eligible = false;
        int horseId = 0;
        long tick = 0L;

        if (screen == null && client.level != null && player != null) {
            Entity vehicle = player.getControlledVehicle();
            if (vehicle instanceof AbstractHorse horse && horse.getControllingPassenger() == player) {
                eligible = true;
                horseId = horse.getId();
                tick = client.level.getGameTime();
            }
        }

        HorseAutodriveController.Output output = HorseAutodriveController.INSTANCE.tick(
                tick,
                eligible,
                horseId,
                this.up,
                this.down,
                this.left,
                this.right,
                this.forwardImpulse,
                this.leftImpulse
        );

        this.up = output.forwardDown();
        this.down = output.backDown();
        this.left = output.leftDown();
        this.right = output.rightDown();
        this.forwardImpulse = output.forwardImpulse();
        this.leftImpulse = output.leftImpulse();
    }
}
