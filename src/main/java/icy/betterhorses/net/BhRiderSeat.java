package icy.betterhorses.net;

import icy.betterhorses.net.client.BhClientCaches;

import icy.betterhorses.net.entity.PercheronHorse;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BhRiderSeat {

    public static final double REAR_CAMERA_FOLLOW = 0.64D;

    private static final double LARGE_SEAT_LIFT = 0.25D;
    private static final double PLAYER_SEAT_DROP = 0.6D;
    private static final double PLAYER_RIDING_OFFSET = 0.35D;
    private static final float HUMANOID_HEIGHT = 1.2F;

    private static final Map<Integer, Vec3> APPLIED = new ConcurrentHashMap<>();

    private BhRiderSeat() {}

    public static double seatLift(AbstractHorse horse) {
        return horse instanceof PercheronHorse ? LARGE_SEAT_LIFT : 0.0D;
    }

    public static double seatDrop(Entity passenger) {
        if (passenger instanceof Player) {
            return PLAYER_SEAT_DROP;
        }
        double offset = passenger.getMyRidingOffset();
        if (offset < 0.0D) {
            return PLAYER_SEAT_DROP - PLAYER_RIDING_OFFSET - offset;
        }
        if (offset == 0.0D && passenger instanceof LivingEntity && passenger.getBbHeight() >= HUMANOID_HEIGHT) {
            return passenger.getBbHeight() / 3.0D;
        }
        return 0.0D;
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
