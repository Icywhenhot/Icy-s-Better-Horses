package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

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

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.JUMP_STRENGTH, 0.47D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }
}
