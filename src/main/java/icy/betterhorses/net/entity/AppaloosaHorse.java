package icy.betterhorses.net.entity;

import icy.betterhorses.net.BreedArchetype;
import icy.betterhorses.net.HorseBreed;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

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

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BreedArchetype.WESTERN);
    }
}
