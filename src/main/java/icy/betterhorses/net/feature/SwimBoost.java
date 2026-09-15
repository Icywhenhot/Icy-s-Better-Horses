package icy.betterhorses.net.feature;

import icy.betterhorses.net.IHorseData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

public final class SwimBoost implements HorseFeature {

    private static final ResourceLocation SWIM_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("icys-better-horses", "swim_speed");
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

        @Nullable AttributeInstance swimSpeed = horse.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (swimSpeed != null && swimSpeed.getModifier(SWIM_SPEED_ID) == null) {
            swimSpeed.addTransientModifier(new AttributeModifier(
                    SWIM_SPEED_ID, SWIM_BONUS, AttributeModifier.Operation.ADD_VALUE));
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

        double depth = horse.getFluidTypeHeight(NeoForgeMod.WATER_TYPE.value());
        if (depth <= WATERLINE) {
            return;
        }

        double lift = Math.min((depth - WATERLINE) * LIFT_PER_BLOCK, LIFT_CAP);
        horse.setDeltaMovement(motion.x, Math.min(motion.y + lift, LIFT_CAP), motion.z);
        horse.resetFallDistance();
    }
}
