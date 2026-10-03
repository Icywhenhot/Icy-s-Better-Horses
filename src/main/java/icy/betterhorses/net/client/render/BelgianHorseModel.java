package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BelgianHorse;
import net.minecraft.client.model.geom.ModelPart;

public class BelgianHorseModel extends BhHorseModel<BelgianHorse> {

    private static final float SHOULDER_HOLD = 0.55F;

    private static final float REACH_SCALE = 0.80F;

    public BelgianHorseModel(ModelPart root) {
        super(root);
    }

    @Override
    protected float gaitShoulderHold(boolean front) {
        return SHOULDER_HOLD;
    }

    @Override
    protected float gaitReachScale(boolean front) {
        return REACH_SCALE;
    }
}
