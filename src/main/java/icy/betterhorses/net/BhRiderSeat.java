package icy.betterhorses.net;

import icy.betterhorses.net.client.BhClientCaches;

import icy.betterhorses.net.entity.PercheronHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BhRiderSeat {

    public static final double REAR_CAMERA_FOLLOW = 0.64D;

    private static final double LARGE_SEAT_LIFT = 0.25D;

    public static final double CART_CAMERA_LIFT = 1.0D;

    private static final Map<Integer, Vec3> APPLIED = new ConcurrentHashMap<>();

    private BhRiderSeat() {}

    public static double seatLift(AbstractHorse horse) {
        return horse instanceof PercheronHorse ? LARGE_SEAT_LIFT : 0.0D;
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
