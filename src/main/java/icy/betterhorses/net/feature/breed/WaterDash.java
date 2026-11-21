package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class WaterDash {

    private static final Tune COOLDOWN = Tune.of("drowned_rider", "dash_cooldown", 200.0D).within(0.0D, 72000.0D);
    private static final Tune DASH_TICKS = Tune.of("drowned_rider", "dash_ticks", 8.0D).within(1.0D, 200.0D);
    private static final Tune DASH_SPEED = Tune.of("drowned_rider", "dash_speed", 0.9D).within(0.0D, 10.0D);

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
        horse.setDeltaMovement(rider.getLookAngle().scale(DASH_SPEED.get()));
        horse.hurtMarked = true;
        if (rider instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(horse));
        }
        dash--;
        return true;
    }

    void start() {
        cooldown = COOLDOWN.ticks();
        dash = DASH_TICKS.ticks();
    }

    void reset() {
        dash = 0;
    }

    static int span() {
        return DASH_TICKS.ticks();
    }

    static int rest() {
        return COOLDOWN.ticks();
    }

    int saved() {
        return cooldown;
    }

    void load(int value) {
        cooldown = Math.clamp(value, 0, COOLDOWN.ticks());
        dash = 0;
    }
}
