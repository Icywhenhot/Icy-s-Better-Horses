package icy.betterhorses.net.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import icy.betterhorses.net.BhHorseKind;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class VehicleStepMixin {

    @WrapOperation(method = "handleMoveVehicle", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void bh_replayLikeTheClient(Entity horse, MoverType type, Vec3 want, Operation<Void> move,
                                        ServerboundMoveVehiclePacket packet) {
        double x = horse.getX();
        double y = horse.getY();
        double z = horse.getZ();
        move.call(horse, type, want);
        if (!BhHorseKind.managed(horse)) {
            return;
        }
        double missX = packet.getX() - horse.getX();
        double missZ = packet.getZ() - horse.getZ();
        double miss = missX * missX + missZ * missZ;
        if (miss <= 0.0625D) {
            return;
        }

        double fx = horse.getX();
        double fy = horse.getY();
        double fz = horse.getZ();
        boolean grounded = horse.onGround();
        horse.setPos(x, y, z);
        horse.setOnGround(true);
        move.call(horse, type, new Vec3(want.x, Math.min(want.y, -0.08D), want.z));
        double retryX = packet.getX() - horse.getX();
        double retryZ = packet.getZ() - horse.getZ();
        if (retryX * retryX + retryZ * retryZ >= miss) {
            horse.setPos(fx, fy, fz);
            horse.setOnGround(grounded);
        }
    }
}
