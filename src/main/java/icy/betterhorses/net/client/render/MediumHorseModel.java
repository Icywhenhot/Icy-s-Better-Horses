package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.MediumHorse;
import net.minecraft.client.model.geom.ModelPart;

public class MediumHorseModel<T extends MediumHorse> extends BhHorseModel<T> {

    public MediumHorseModel(ModelPart root) {
        super(root);
    }
}
