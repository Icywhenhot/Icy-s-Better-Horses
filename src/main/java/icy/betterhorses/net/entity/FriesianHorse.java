package icy.betterhorses.net.entity;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;

public class FriesianHorse extends BhBreedHorse {

    public static final float WIDTH = 1.3964844F;
    public static final float HEIGHT = 1.8F;

    public FriesianHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public ResourceKey<BreedType> bhFixedBreed() {
        return BhContent.FRIESIAN.key();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BhContent.WAR.value());
    }
}
