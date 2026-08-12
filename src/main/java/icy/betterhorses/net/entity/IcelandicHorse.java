package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Icelandic horse: short, shaggy, cold-hardy, and not fast.
 *
 * <p>Four coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}.
 */
public class IcelandicHorse extends BhBreedHorse {

    // Icelandics are ponies: shorter and narrower than a vanilla horse (1.3964844 x 1.6).
    public static final float WIDTH = 1.2F;
    public static final float HEIGHT = 1.45F;

    public IcelandicHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.ICELANDIC;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.ICELANDIC;
    }

    /**
     * Cold-hardy and sure-footed, but slow. Conservative for a first pass; these belong in a
     * per-breed traits table once there are enough breeds to balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.JUMP_STRENGTH, 0.6D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    // breeding true is handled by BhBreedHorse.getBreedOffspring for every breed at once
}
