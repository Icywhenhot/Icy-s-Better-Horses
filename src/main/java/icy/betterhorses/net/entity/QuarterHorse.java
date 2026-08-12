package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Quarter Horse: named for outrunning anything over a quarter mile.
 *
 * <p>Three coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}.
 */
public class QuarterHorse extends MediumHorse {

    public QuarterHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.QUARTER;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.QUARTER;
    }

    /**
     * A sprinter: second only to the Thoroughbred for pace, and pays for it in jump and
     * health. Same caveat as the other breeds - these belong in a per-breed traits table
     * once there are enough breeds to balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.JUMP_STRENGTH, 0.66D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
