package icy.betterhorses.net.client.render;

import icy.betterhorses.net.entity.SmallHorse;
import net.minecraft.client.model.geom.ModelPart;

public class SmallHorseModel<T extends SmallHorse> extends BhHorseModel<T> {

    public SmallHorseModel(ModelPart root) {
        super(root);
    }
}
