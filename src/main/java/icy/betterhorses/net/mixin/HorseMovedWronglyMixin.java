package icy.betterhorses.net.mixin;

import icy.betterhorses.net.BhFeature;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class HorseMovedWronglyMixin {

    // Vanilla snaps a ridden horse back on stairs and hills (MC-100830); allow 0.6 blocks instead of 0.25.
    private static final double BH_HORSE_TOLERANCE_SQ = 0.36D;

    @Shadow public ServerPlayer player;

    // Ordinal 1 is the "moved wrongly" distance check; 0 and 2 are collision-box shrinks.
    @ModifyConstant(method = "handleMoveVehicle", constant = @Constant(doubleValue = 0.0625D, ordinal = 1))
    private double bh_horseMoveTolerance(double original) {
        if (this.player.getRootVehicle() instanceof AbstractHorse && BhFeature.SMOOTH_HORSE_CLIMBING.on()) {
            return BH_HORSE_TOLERANCE_SQ;
        }
        return original;
    }
}
