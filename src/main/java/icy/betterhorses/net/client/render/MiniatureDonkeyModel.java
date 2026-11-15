package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

public class MiniatureDonkeyModel extends DonkeyModel {

    private static final float REACH = 0.5F;

    public MiniatureDonkeyModel(ModelPart root) {
        super(root);
    }

    @Override
    protected float gaitReachScale(boolean front) {
        return REACH;
    }
}
