package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Thoroughbred: the racehorse. Fastest of the breeds so far, and the most fragile.
 *
 * <p>Three coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}.
 */
public class ThoroughbredHorse extends MediumHorse {

    public ThoroughbredHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.THOROUGHBRED;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.THOROUGHBRED;
    }

    /**
     * Bred for one thing: speed on flat ground. Pays for it in health and in a jump that
     * does not match its pace. Same caveat as the other breeds - these belong in a
     * per-breed traits table once there are enough breeds to balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3375D)
                .add(Attributes.JUMP_STRENGTH, 0.7D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
