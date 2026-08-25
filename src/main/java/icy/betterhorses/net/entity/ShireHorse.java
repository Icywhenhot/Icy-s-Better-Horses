package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * England's largest draft horse, and the tallest thing in the mod.
 *
 * <p>WIDTH is the Percheron's unchanged: the two share a barrel box, 12 wide, and
 * the extra unit the Shire carries on each side is leg feathering, which is hair
 * rather than something a collision box should catch on.
 *
 * <p>HEIGHT is derived, not chosen. The Shire mesh is the Percheron's raised 3
 * model units, so its hitbox is the Percheron's 1.95 plus 3/16 of a block.
 */
public class ShireHorse extends BhBreedHorse {

    public static final float WIDTH = 1.5234375F;
    public static final float HEIGHT = 2.1375F;

    public ShireHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.SHIRE;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.SHIRE;
    }

    /**
     * Heavier and steadier than the Percheron: more health, a little less speed
     * and a little less jump. Same STEP_HEIGHT -- both are tall enough to walk
     * over a block.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.JUMP_STRENGTH, 0.47D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
