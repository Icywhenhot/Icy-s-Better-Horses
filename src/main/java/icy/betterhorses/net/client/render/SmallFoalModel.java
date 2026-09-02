package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

public class SmallFoalModel extends SmallHorseModel {

    private static final float STRIDE = 0.55F;

    private static final float FRONT_HOLD = 0.55F;
    private static final float BACK_HOLD = 0.30F;

    private static final float FRONT_REACH = 0.30F;
    private static final float BACK_REACH = 0.70F;

    public SmallFoalModel(ModelPart root) {
        super(root);
    }

    @Override
    protected float gaitScale(boolean front) {
        return STRIDE;
    }

    @Override
    protected float gaitShoulderHold(boolean front) {
        return front ? FRONT_HOLD : BACK_HOLD;
    }

    @Override
    protected float gaitReachScale(boolean front) {
        return front ? FRONT_REACH : BACK_REACH;
    }
}
