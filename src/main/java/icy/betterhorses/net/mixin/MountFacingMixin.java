package icy.betterhorses.net.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MountFacingMixin {

    @Inject(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void bh_turnHorseToRider(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!(player.getVehicle() instanceof AbstractHorse horse) || horse.getControllingPassenger() != player) {
            return;
        }
        float yaw = player.getYRot();
        horse.setYRot(yaw);
        horse.yRotO = yaw;
        horse.setYBodyRot(yaw);
        horse.yBodyRotO = yaw;
        horse.setYHeadRot(yaw);
        horse.yHeadRotO = yaw;
    }
}
