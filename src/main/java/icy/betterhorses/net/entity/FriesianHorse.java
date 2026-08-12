package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Friesian: a tall black baroque carriage horse with feathered legs.
 *
 * <p>The opposite build to the {@link IcelandicHorse} in every way, which makes the pair a
 * useful test of the shared rig - the model is uniformly 1.25x the Icelandic's and not one
 * animation constant had to change.
 *
 * <p>Two coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}.
 */
public class FriesianHorse extends BhBreedHorse {

    // Derived from the model rather than picked. The barrel is 10px across, the same as
    // vanilla's horse, so the width is vanilla's; the withers sit at bb y=25 against the
    // Icelandic's 20, so passengerAttachments below lands just above the barrel exactly as
    // it does for the Icelandic. 1.8 also makes a Friesian as tall as a player, which is a
    // useful thing to be able to picture.
    public static final float WIDTH = 1.3964844F;
    public static final float HEIGHT = 1.8F;

    public FriesianHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.FRIESIAN;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.FRIESIAN;
    }

    /**
     * Heavy, strong and steady rather than quick: more health and more carrying presence
     * than an Icelandic, a little more speed, but nothing like a sprinter's jump. Same
     * caveat as the Icelandic - these belong in a per-breed traits table once there are
     * enough breeds to balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.225D)
                .add(Attributes.JUMP_STRENGTH, 0.65D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    // breeding true is handled by BhBreedHorse.getBreedOffspring for every breed at once
}
