package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * The Belgian Draft, the third breed on the large rig.
 *
 * <p>The gait constants are the Percheron's and the Shire's, unchanged. The
 * Belgian's mesh differs from the Percheron's only in x -- a block wider through
 * the barrel, neck and head, with the leg columns carried 0.5 outward -- and
 * neither of these numbers reads x: {@code gaitShoulderHold} is how much of the
 * shoulder stays put fore-and-aft, {@code gaitReachScale} how far the hoof
 * travels. A wider horse takes the same length of stride.
 */
public class BelgianHorseModel extends BhHorseModel {

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
