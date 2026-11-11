package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

public class UndeadHorseModel extends BhHorseModel {

    private static final float DRAFT_SHOULDER_HOLD = 0.55F;

    private static final float DRAFT_REACH_SCALE = 0.80F;

    private final boolean draft;

    public UndeadHorseModel(ModelPart root, boolean draft) {
        super(root);
        this.draft = draft;
    }

    @Override
    protected float gaitShoulderHold(boolean front) {
        return draft ? DRAFT_SHOULDER_HOLD : super.gaitShoulderHold(front);
    }

    @Override
    protected float gaitReachScale(boolean front) {
        return draft ? DRAFT_REACH_SCALE : super.gaitReachScale(front);
    }
}
