package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * The Belgian foal.
 *
 * <p>The same mesh as the Percheron and Shire foals -- {@code belgian
 * baby.bbmodel} is byte-for-byte the Percheron's file -- so the same gait: it
 * reads {@link BhLargeFoalGait} rather than carrying its own copy of the
 * numbers. This class exists only because {@code AbstractHorseRenderer} types
 * its adult and baby models to one class, so the Belgian's baby must be a
 * {@link BelgianHorseModel}.
 */
public class BelgianFoalModel extends BelgianHorseModel {

    public BelgianFoalModel(ModelPart root) {
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
