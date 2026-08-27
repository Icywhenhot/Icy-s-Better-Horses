package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

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

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 38.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.175D)
                .add(Attributes.JUMP_STRENGTH, 0.45D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
