package icy.betterhorses.net;

import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class BhHorseSteering {

    private static final double FRONT_PASSENGER_Z_OFFSET = 0.35D;
    private static final double REAR_PASSENGER_Z_OFFSET = -0.35D;
    private static final float FREE_CAMERA_ANGLE_THRESHOLD = 90.0F;
    private static final float MANUAL_TURN_DEGREES_PER_TICK = 4.5F;
    private static final float MANUAL_TURN_FULL_SPEED = 0.35F;
    private static final float MANUAL_TURN_SPEED_FALLOFF = 0.45F;

    private BhHorseSteering() {}

    public static int benchSeatIndex(AbstractHorse horse, Entity passenger) {
        if (!(passenger instanceof Player)) {
            return 1;
        }
        int seat = 0;
        for (Entity other : horse.getPassengers()) {
            if (other == passenger) {
                break;
            }
            if (other instanceof Player) {
                seat++;
            }
        }
        return Math.min(seat, 1);
    }

    public static @Nullable Vec3 multiRiderOffset(AbstractHorse horse, Entity passenger) {
        if (!BhConfig.multiRidingEnabled() || horse.getPassengers().size() <= 1) {
            return null;
        }

        int passengerIndex = horse.getPassengers().indexOf(passenger);
        if (passengerIndex < 0) {
            return null;
        }

        double zOffset = passengerIndex == 0 ? FRONT_PASSENGER_Z_OFFSET : REAR_PASSENGER_Z_OFFSET;
        return new Vec3(0.0D, 0.0D, zOffset).yRot(-horse.getYRot() * ((float) Math.PI / 180.0F));
    }

    public static @Nullable Vec2 riddenRotation(AbstractHorse horse, IHorseData data, Player player) {
        if (data.bh_isFreeSteer()) {
            float speed = (float) horse.getDeltaMovement().horizontalDistance();
            float turnScale = 1.0F - MANUAL_TURN_SPEED_FALLOFF
                    * Mth.clamp(speed / MANUAL_TURN_FULL_SPEED, 0.0F, 1.0F);
            float yaw = horse.getYRot() - player.xxa * MANUAL_TURN_DEGREES_PER_TICK * turnScale;
            return new Vec2(0.0F, Mth.wrapDegrees(yaw));
        }

        if (player.xxa != 0.0F || player.zza != 0.0F) {
            return null;
        }

        float playerYRot = Mth.wrapDegrees(player.getYRot());
        float rotationDifference = Mth.wrapDegrees(playerYRot - horse.getYRot());

        if (Math.abs(rotationDifference) > FREE_CAMERA_ANGLE_THRESHOLD) {
            float horseYRot = Mth.wrapDegrees(
                    playerYRot - Math.signum(rotationDifference) * FREE_CAMERA_ANGLE_THRESHOLD);
            return new Vec2(player.getXRot() * 0.5F, horseYRot);
        }

        return new Vec2(player.getXRot() * 0.5F, horse.getYRot());
    }

    public static boolean canAddPassenger(AbstractHorse horse, IHorseData data, Entity passenger) {
        List<Entity> passengers = horse.getPassengers();
        boolean multiRidingEnabled = BhConfig.multiRidingEnabled() || data.bh_hasCartGear();
        boolean horseExclusivityEnabled = BhConfig.horseExclusivityEnabled();
        if (passengers.size() >= (multiRidingEnabled ? 2 : 1)) {
            return false;
        }

        if (!(passenger instanceof Player)) {
            return data.bh_hasCartGear()
                    && !passengers.isEmpty()
                    && passengers.get(0) instanceof Player
                    && HorseCartEntity.isCarriableCargo(passenger);
        }

        UUID owner = data.bh_getOwner();
        if (owner == null || !horseExclusivityEnabled) {
            if (passengers.isEmpty()) {
                return true;
            }
            return multiRidingEnabled;
        }

        Player player = (Player) passenger;
        boolean mayDrive = data.bh_maySaddleUp(player.getUUID());
        if (passengers.isEmpty()) {
            return mayDrive;
        }
        if (!multiRidingEnabled) {
            return false;
        }
        if (mayDrive) {
            return true;
        }
        return data.bh_maySaddleUp(passengers.get(0).getUUID());
    }
}
