package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * The Clydesdale, the fourth breed on the large rig.
 *
 * <p>The gait constants are the Percheron's and the Shire's, unchanged. This
 * mesh IS the Percheron's plus feathering, and feathering is hair hanging off
 * the leg it is already attached to: neither how much of the shoulder stays put
 * nor how far the hoof travels has any reason to move.
 */
public class ClydesdaleHorseModel extends BhHorseModel {

    private static final float SHOULDER_HOLD = 0.55F;

    private static final float REACH_SCALE = 0.80F;

    public ClydesdaleHorseModel(ModelPart root) {
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
