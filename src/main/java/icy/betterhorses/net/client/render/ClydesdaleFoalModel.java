package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * The Clydesdale foal.
 *
 * <p>The shared large-breed foal, the same mesh the Percheron, Shire and
 * Belgian foals use, so the same gait: it reads {@link BhLargeFoalGait} rather
 * than carrying its own copy of the numbers. This class exists only because
 * {@code AbstractHorseRenderer} types its adult and baby models to one class.
 *
 * <p>The adult's feathering does not reach the foal and cannot -- that mesh has
 * no feather boxes and no fringe cubes. A bare-legged Clydesdale foal is also
 * true to life.
 */
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
