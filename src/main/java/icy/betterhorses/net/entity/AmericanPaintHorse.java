package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The American Paint: a stock horse carrying broad white patches over a solid base.
 *
 * <p>Three coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}. The white patching is painted into each coat rather than
 * layered on as vanilla markings.
 */
public class AmericanPaintHorse extends MediumHorse {

    public AmericanPaintHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.AMERICAN_PAINT;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.AMERICAN_PAINT;
    }

    /**
     * The all-rounder of the three: no standout number, nothing weak either. Same caveat
     * as the other breeds - these belong in a per-breed traits table once there are enough
     * breeds to balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2825D)
                .add(Attributes.JUMP_STRENGTH, 0.7D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
