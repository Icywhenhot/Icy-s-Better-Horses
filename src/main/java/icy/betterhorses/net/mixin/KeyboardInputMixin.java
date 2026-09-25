package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.client.HorseAutodriveController;
import icy.betterhorses.net.client.HorseGearController;
import icy.betterhorses.net.client.HorseFreeLookController;
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

    @Inject(method = "tick(ZF)V", at = @At("TAIL"))
    private void bh_applyHorseAutodrive(boolean sneaking, float sneakingSpeedMultiplier, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        Screen screen = client.screen;

        boolean mounted = false;
        int horseId = 0;
        long tick = 0L;
        AbstractHorse riddenHorse = null;

        if (client.level != null && player != null) {
            Entity vehicle = player.getControlledVehicle();
            if (vehicle instanceof AbstractHorse horse && BhHorseKind.managed(horse)
                    && horse.getControllingPassenger() == player) {
                mounted = true;
                horseId = horse.getId();
                riddenHorse = horse;
                tick = client.level.getGameTime();
            }
        }
        boolean eligible = mounted && screen == null;

        HorseFreeLookController.INSTANCE.tick(eligible ? riddenHorse : null);

        HorseAutodriveController.Output output = HorseAutodriveController.INSTANCE.tick(
                tick,
                mounted,
                horseId,
                this.up,
                this.down,
                this.left,
                this.right,
                this.forwardImpulse,
                this.leftImpulse
        );

        boolean forwardDown = output.forwardDown();
        float forwardImpulse = output.forwardImpulse();
        float leftImpulse = output.leftImpulse();
        if (output.active()) {
            HorseGearController.INSTANCE.reset();
        } else if (HorseGearController.INSTANCE
                .tick(mounted, riddenHorse, this.up, this.down)
                .geared()) {
            forwardDown = true;
            forwardImpulse = 1.0F;
        }

        this.up = forwardDown;
        this.down = output.backDown();
        this.left = output.leftDown();
        this.right = output.rightDown();
        this.leftImpulse = leftImpulse;
        this.forwardImpulse = forwardImpulse;
    }
}
