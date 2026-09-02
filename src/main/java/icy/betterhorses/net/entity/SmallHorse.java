package icy.betterhorses.net.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.level.Level;

public abstract class SmallHorse extends BhBreedHorse {

    public static final float WIDTH = 1.2F;
    public static final float HEIGHT = 1.55F;

    protected SmallHorse(EntityType<? extends Horse> type, Level level) {
        super(type, level);
    }
}
