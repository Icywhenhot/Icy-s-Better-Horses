package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Icelandic horse model.
 *
 * <p>All the motion lives in {@link BhHorseModel}: the animator works in offsets from the
 * rest pose baked into whichever layer is passed in, so it needs to know nothing about
 * which breed it is driving. This subclass exists to give the renderer and its tack layers
 * a concrete type — and to be the place a genuinely Icelandic-only behaviour would go, such
 * as the tölt, if one is ever added.
 *
 * <p>The saddle, armour and chest layers instantiate <em>this same class</em> over their own
 * baked geometry. That is what guarantees the tack runs the identical animator and cannot
 * drift off the barrel.
 */
public class IcelandicHorseModel extends BhHorseModel {

    public IcelandicHorseModel(ModelPart root) {
        super(root);
    }
}
