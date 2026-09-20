package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SwimBoost implements HorseFeature {

    private static final UUID SWIM_SPEED_ID = UUID.fromString("5b0a1e4c-7d33-4d1f-9a2e-6c81f0b3d417");
    private static final double SWIM_BONUS = 0.30D;

    private static final double WATERLINE = 1.0D;
    private static final double LIFT_PER_BLOCK = 0.10D;
    private static final double LIFT_CAP = 0.08D;

    private static final double STEP_RISE = 0.15D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (!horse.isInWater()) {
            return;
        }

        // TODO: swim speed bonus disabled until the attribute is ported.
        @Nullable AttributeInstance swimSpeed = null;
        if (swimSpeed != null && swimSpeed.getModifier(SWIM_SPEED_ID) == null) {
            swimSpeed.addTransientModifier(new AttributeModifier(
                    SWIM_SPEED_ID, "bh_swim_speed", SWIM_BONUS, AttributeModifier.Operation.ADDITION));
        }

        if (horse.onGround()) {
            return;
        }

        Vec3 motion = horse.getDeltaMovement();

        if (horse.horizontalCollision) {
            horse.setDeltaMovement(motion.x, Math.max(motion.y, STEP_RISE), motion.z);
            horse.resetFallDistance();
            return;
        }

        // TODO: fluid-depth lift disabled until the attribute is ported.
        double depth = 0.0D;
        if (depth <= WATERLINE) {
            return;
        }

        double lift = Math.min((depth - WATERLINE) * LIFT_PER_BLOCK, LIFT_CAP);
        horse.setDeltaMovement(motion.x, Math.min(motion.y + lift, LIFT_CAP), motion.z);
        horse.resetFallDistance();
    }
}
