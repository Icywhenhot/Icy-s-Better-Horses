package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Clydesdale: the Percheron's frame under a great deal of feather.
 *
 * <p>Both numbers are the Percheron's, and neither is a shortcut. The mesh is
 * that breed's twenty-three cubes byte for byte, so nothing in the barrel or
 * the legs moved; and the feathering that was added sits inside the leg's own
 * x span and hangs off the back of it. It is hair either way -- the same
 * reasoning the Shire's comment gives for not counting its own.
 */
public class ClydesdaleHorse extends BhBreedHorse {

    public static final float WIDTH = 1.5234375F;
    public static final float HEIGHT = 1.95F;

    public ClydesdaleHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.CLYDESDALE;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.CLYDESDALE;
    }

    /**
     * The athlete of the four drafts. Clydesdales are carriage and parade
     * horses before they are ploughing ones, so this is the quickest and the
     * best over a fence of the set, and pays for it in health: the Belgian
     * pulls harder, the Shire carries more.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.JUMP_STRENGTH, 0.52D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
