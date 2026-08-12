package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Andalusian: the Spanish baroque horse. Collected, agile, and built to turn.
 *
 * <p>Three coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}.
 */
public class AndalusianHorse extends MediumHorse {

    public AndalusianHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.ANDALUSIAN;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.ANDALUSIAN;
    }

    /**
     * A schooled riding horse: quick enough, jumps well, holds up. Same caveat as the other
     * breeds - these belong in a per-breed traits table once there are enough breeds to
     * balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.JUMP_STRENGTH, 0.72D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
