package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

public class ClydesdaleFoalModel extends ClydesdaleHorseModel {

    public ClydesdaleFoalModel(ModelPart root) {
        super(root);
    }

    @Override
    protected float gaitScale(boolean front) {
        return BhLargeFoalGait.STRIDE;
    }

    @Override
    protected float gaitShoulderHold(boolean front) {
        return front ? BhLargeFoalGait.FRONT_HOLD : BhLargeFoalGait.BACK_HOLD;
    }

    @Override
    protected float gaitReachScale(boolean front) {
        return front ? BhLargeFoalGait.FRONT_REACH : BhLargeFoalGait.BACK_REACH;
    }
}
