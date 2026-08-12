package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Appaloosa: the spotted Nez Perce horse. Tough, sure-footed high-country stock.
 *
 * <p>Four coats, taken from the texture files rather than from vanilla coat genetics -
 * see {@link BhBreedCoats}. The spots are painted into each coat, so unlike a vanilla
 * horse there is no separate markings layer to combine.
 */
public class AppaloosaHorse extends MediumHorse {

    public AppaloosaHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.APPALOOSA;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.APPALOOSA;
    }

    /**
     * Hardy and a strong jumper, bred over rough country, but not quick. Same caveat as
     * the other breeds - these belong in a per-breed traits table once there are enough
     * breeds to balance against each other.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.245D)
                .add(Attributes.JUMP_STRENGTH, 0.75D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
