package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

public class LargeDonkeyModel extends DonkeyModel {

    private static final float SHOULDER_HOLD = 1.0F;

    private static final float REACH = 0.5F;

    public LargeDonkeyModel(ModelPart root) {
        super(root);
    }

    @Override
    protected float gaitShoulderHold(boolean front) {
        return SHOULDER_HOLD;
    }

    @Override
    protected float gaitReachScale(boolean front) {
        return REACH;
    }
}
