package icy.betterhorses.net.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class VehicleStepMixin {

    @Shadow public ServerPlayer player;

    @ModifyConstant(method = "handleMoveVehicle", constant = @Constant(doubleValue = 0.0625D))
    private double bh_horseMissTolerance(double original) {
        return this.player.getRootVehicle() instanceof AbstractHorse ? 0.36D : original;
    }
}
