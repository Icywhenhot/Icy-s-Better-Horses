package icy.betterhorses.net.mixin;

import icy.betterhorses.net.client.HorseAutodriveController;
import icy.betterhorses.net.client.HorseGearController;
import icy.betterhorses.net.client.HorseFreeLookController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {

    @Inject(method = "tick", at = @At("TAIL"))
    private void bh_applyHorseAutodrive(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        Screen screen = client.gui.screen();

        boolean eligible = false;
        int horseId = 0;
        long tick = 0L;
        AbstractHorse riddenHorse = null;

        if (screen == null && client.level != null && player != null) {
            Entity vehicle = player.getControlledVehicle();
            if (vehicle instanceof AbstractHorse horse && horse.getControllingPassenger() == player) {
                eligible = true;
                horseId = horse.getId();
                riddenHorse = horse;
                tick = client.level.getGameTime();
            }
        }

        HorseFreeLookController.INSTANCE.tick(eligible ? riddenHorse : null);

        Input current = this.keyPresses;
        Vec2 currentMove = this.moveVector;
        HorseAutodriveController.Output output = HorseAutodriveController.INSTANCE.tick(
                tick,
                eligible,
                horseId,
                current.forward(),
                current.backward(),
                current.left(),
                current.right(),
                currentMove.y,
                currentMove.x
        );

        boolean forwardDown = output.forwardDown();
        float forwardImpulse = output.forwardImpulse();
        float leftImpulse = output.leftImpulse();
        if (output.active()) {
            HorseGearController.INSTANCE.reset();
        } else if (HorseGearController.INSTANCE
                .tick(eligible, riddenHorse, current.forward(), current.backward())
                .geared()) {
            forwardDown = true;
            forwardImpulse = 1.0F;
        }

        this.keyPresses = new Input(
                forwardDown,
                output.backDown(),
                output.leftDown(),
                output.rightDown(),
                current.jump(),
                current.shift(),
                current.sprint()
        );
        this.moveVector = new Vec2(leftImpulse, forwardImpulse);
    }
}
