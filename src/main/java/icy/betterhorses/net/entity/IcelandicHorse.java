package icy.betterhorses.net.entity;

import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

public class IcelandicHorse extends BhBreedHorse {

    public static final float WIDTH = 1.2F;
    public static final float HEIGHT = 1.45F;

    public IcelandicHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public HorseBreed bhFixedBreed() {
        return HorseBreed.ICELANDIC;
    }

    @Override
    public BhBreedCoats bhCoats() {
        return BhBreedCoats.ICELANDIC;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BreedArchetype.PONY);
    }

}
