package icy.betterhorses.net.entity;

import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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
        return bhAttributes(BreedArchetype.DRAFT);
    }
}
