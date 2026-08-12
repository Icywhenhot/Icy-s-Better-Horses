package icy.betterhorses.net.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * Base for the medium size class: Appaloosa, Thoroughbred and American Paint.
 *
 * <p>These three are ordinary riding horses that differ only in colour, so Icy models them
 * as <em>one mesh with three texture sets</em> — the three {@code .bbmodel}s are
 * geometrically byte-identical. Everything that follows from the mesh is therefore shared:
 * one body geometry, one saddle, one armour, one chest, one stabilizer, one set of tack
 * textures. What stays per-breed is the coat list and the stat block.
 *
 * <p>That split is the pattern the remaining breeds should follow. A "size class" owns the
 * geometry and the tack; a breed owns its coats and how it feels to ride.
 */
public abstract class MediumHorse extends BhBreedHorse {

    // Derived, not chosen. The barrel is 10px across and its top sits at bb y=23, so
    // passengerAttachments below (HEIGHT * 0.90) lands at 23.04px — right on the barrel,
    // the same relationship the Icelandic and Friesian use. Working it out lands exactly
    // on vanilla's own horse dimensions, which is the confirmation that it is right:
    // these are the plain riding horses vanilla's hitbox was built for.
    public static final float WIDTH = 1.3964844F;
    public static final float HEIGHT = 1.6F;

    protected MediumHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }
}
