package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.BhBreedMule;
import net.minecraft.client.model.geom.ModelPart;

public class MuleModel extends MediumHorseModel<BhBreedMule> {

    private static final float SHOULDER_HOLD = 1.0F;

    private static final float REACH = 0.5F;

    public MuleModel(ModelPart root) {
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
