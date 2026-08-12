package icy.betterhorses.net.client.render;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Model for the medium size class.
 *
 * <p>One class for all three medium breeds, because their {@code .bbmodel}s are
 * geometrically byte-identical — the same shared jem round-trips against all three — so
 * there is nothing for a per-breed subclass to hold. The breed shows up only in which coat
 * texture the renderer binds.
 *
 * <p>All the motion lives in {@link BhHorseModel}, unchanged. The saddle, armour and chest
 * layers instantiate this same class over their own baked geometry, so the tack cannot
 * drift off the barrel.
 */
public class MediumHorseModel extends BhHorseModel {

    public MediumHorseModel(ModelPart root) {
        super(root);
    }
}
