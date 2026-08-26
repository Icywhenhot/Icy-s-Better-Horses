package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

/**
 * The Belgian Draft: the broadest horse in the mod, and the strongest puller.
 *
 * <p>Both numbers are derived by the rule the Shire already set -- the hitbox
 * moves by the mesh delta over 16. The Belgian mesh is the Percheron's widened
 * one model unit through the barrel (12 to 13, half a unit either side) and
 * unchanged in y and z, so WIDTH is the Percheron's 1.5234375 plus 1/16 of a
 * block and HEIGHT is the Percheron's untouched.
 *
 * <p>The feathering is deliberately not counted, for the reason the Shire's
 * comment gives: it is hair, not something a collision box should catch on.
 */
public class BelgianHorse extends BhBreedHorse {

    public static final float WIDTH = 1.5859375F;
    public static final float HEIGHT = 1.95F;

    public BelgianHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.BELGIAN;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.BELGIAN;
    }

    /**
     * The heaviest of the three drafts. More health than the Shire, and slower
     * and lower over a fence than either it or the Percheron -- the Belgian is
     * the pulling horse of the set, not the moving one.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 38.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.175D)
                .add(Attributes.JUMP_STRENGTH, 0.45D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
