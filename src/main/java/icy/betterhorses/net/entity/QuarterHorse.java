package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class QuarterHorse extends MediumHorse {

    private static final double SNAP_SPEED_FACTOR = 1.8D;

    public QuarterHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.QUARTER;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.QUARTER;
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        if (!this.onGround() || this.isStanding() || player.zza <= 0.0F) {
            return;
        }

        double target = this.getAttributeValue(Attributes.MOVEMENT_SPEED) * SNAP_SPEED_FACTOR;
        Vec3 movement = this.getDeltaMovement();
        double speed = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
        if (speed >= target) {
            return;
        }

        double directionX;
        double directionZ;
        if (speed > 1.0E-4D) {
            directionX = movement.x / speed;
            directionZ = movement.z / speed;
        } else {
            double yaw = Math.toRadians(this.getYRot());
            directionX = -Math.sin(yaw);
            directionZ = Math.cos(yaw);
        }
        this.setDeltaMovement(directionX * target, movement.y, directionZ * target);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.JUMP_STRENGTH, 0.66D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
