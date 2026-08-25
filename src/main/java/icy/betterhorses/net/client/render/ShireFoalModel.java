package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * The Shire foal.
 *
 * <p>The same mesh as the Percheron foal, so the same gait: both read
 * {@link BhLargeFoalGait} rather than carrying their own copy of the numbers.
 * This class exists only because {@code AbstractHorseRenderer} types its adult
 * and baby models to one class, so the Shire's baby must be a
 * {@link ShireHorseModel}.
 */
public class ShireFoalModel extends ShireHorseModel {

    public ShireFoalModel(ModelPart root) {
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
