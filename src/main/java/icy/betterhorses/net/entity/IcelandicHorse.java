package icy.betterhorses.net.entity;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BreedType;
import net.minecraft.resources.ResourceKey;
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
    public ResourceKey<BreedType> bhFixedBreed() {
        return BhContent.ICELANDIC.key();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return bhAttributes(BhContent.PONY.value());
    }

}
