package icy.betterhorses.net.entity;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.Level;

public class MorganHorse extends SmallHorse {

    public MorganHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }

    @Override
    public ResourceKey<BreedType> bhFixedBreed() {
        return BhContent.MORGAN.key();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BhContent.WESTERN.value());
    }
}
