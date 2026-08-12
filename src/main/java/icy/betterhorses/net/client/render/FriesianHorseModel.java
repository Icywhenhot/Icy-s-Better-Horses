package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Friesian horse model.
 *
 * <p>All the motion lives in {@link BhHorseModel}, unchanged from the Icelandic. That is the
 * payoff of animating in offsets from the rest pose: the Friesian is a much bigger animal -
 * a 10x10x22 barrel against the Icelandic's 8x9x18, with the withers 7px higher - and not one
 * constant in the animator had to move for it.
 *
 * <p>The saddle, armour and chest layers instantiate <em>this same class</em> over their own
 * baked geometry, so the tack cannot drift off the barrel.
 */
public class FriesianHorseModel extends BhHorseModel {

    public FriesianHorseModel(ModelPart root) {
        super(root);
    }
}
