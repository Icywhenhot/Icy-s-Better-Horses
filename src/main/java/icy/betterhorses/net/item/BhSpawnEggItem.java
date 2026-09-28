package icy.betterhorses.net.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.function.Supplier;

public class BhSpawnEggItem extends ForgeSpawnEggItem {

    public BhSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Properties properties) {
        super(type, background, highlight, properties);
    }

    @Override
    public int getColor(int tintIndex) {
        return -1;
    }
}
