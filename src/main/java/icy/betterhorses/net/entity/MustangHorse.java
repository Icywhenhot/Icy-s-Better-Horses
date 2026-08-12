package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Mustang: the feral horse of the American west. Hardiest of the medium breeds.
 *
 * <p>Three coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}.
 */
public class MustangHorse extends MediumHorse {

    public MustangHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.MUSTANG;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.MUSTANG;
    }

    /**
     * Survived without people looking after it, and the stat block says so: the most health
     * of any medium breed, at the cost of pace. Same caveat as the other breeds - these
     * belong in a per-breed traits table once there are enough breeds to balance against
     * each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2625D)
                .add(Attributes.JUMP_STRENGTH, 0.68D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
