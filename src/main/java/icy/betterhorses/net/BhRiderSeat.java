package icy.betterhorses.net;

import icy.betterhorses.net.client.BhClientCaches;

import icy.betterhorses.net.entity.PercheronHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BhRiderSeat {

    public static final double REAR_CAMERA_FOLLOW = 0.64D;

    private static final double LARGE_SEAT_LIFT = 0.25D;

    private static final double PLAYER_SEAT_DROP = 0.6D;

    // How much of the rear shift the rider's body follows, tuned in game against the breed models.
    public static final double REAR_BODY_FOLLOW_BACK = -0.55D;
    public static final double REAR_BODY_FOLLOW_UP = 1.11D;

    // Players on breed horses sit this far forward so their hands reach the reins.
    public static final double BREED_SEAT_FORWARD = 0.03D;

    private static final Map<Integer, Vec3> APPLIED = new ConcurrentHashMap<>();

    private BhRiderSeat() {}

    public static double seatLift(AbstractHorse horse) {
        return horse instanceof PercheronHorse ? LARGE_SEAT_LIFT : 0.0D;
    }

    public static double seatDrop(net.minecraft.world.entity.Entity passenger) {
        return passenger instanceof net.minecraft.world.entity.player.Player ? PLAYER_SEAT_DROP : 0.0D;
    }

    public static void publish(int horseId, Vec3 shift) {
        if (shift.lengthSqr() == 0.0D) {
            APPLIED.remove(horseId);
        } else {
            APPLIED.put(horseId, shift);
        }
    }

    public static Vec3 applied(int horseId) {
        return APPLIED.getOrDefault(horseId, Vec3.ZERO);
    }

    public static void reset() {
        APPLIED.clear();
    }

    static {
        BhClientCaches.register(BhRiderSeat::reset);
    }
}
