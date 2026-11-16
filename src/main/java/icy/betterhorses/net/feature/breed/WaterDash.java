package icy.betterhorses.net.feature.breed;

import net.minecraft.util.Mth;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

final class WaterDash {

    static final int COOLDOWN = 200;
    static final int DASH_TICKS = 8;

    int cooldown;
    int dash;

    static boolean underwater(AbstractHorse horse, Player rider) {
        return rider != null && horse.isInWater() && rider.isEyeInFluid(FluidTags.WATER);
    }

    boolean push(AbstractHorse horse, Player rider, boolean ready) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (dash <= 0 || !ready) {
            dash = 0;
            return false;
        }
        horse.setDeltaMovement(rider.getLookAngle().scale(0.9D));
        horse.hurtMarked = true;
        if (rider instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(horse));
        }
        dash--;
        return true;
    }

    void start() {
        cooldown = COOLDOWN;
        dash = DASH_TICKS;
    }

    void reset() {
        dash = 0;
    }

    int saved() {
        return cooldown;
    }

    void load(int value) {
        cooldown = Mth.clamp(value, 0, COOLDOWN);
        dash = 0;
    }
}
