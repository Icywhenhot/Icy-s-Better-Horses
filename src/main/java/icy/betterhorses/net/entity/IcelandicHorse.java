package icy.betterhorses.net.entity;

import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

public class IcelandicHorse extends BhBreedHorse {

    public static final float WIDTH = 1.2F;
    public static final float HEIGHT = 1.45F;

    public IcelandicHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    private static final double SURE_FOOTED_HEIGHT = 12.0D;

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.ICELANDIC;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        if (fallDistance < SURE_FOOTED_HEIGHT) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.ICELANDIC;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.JUMP_STRENGTH, 0.6D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

}
