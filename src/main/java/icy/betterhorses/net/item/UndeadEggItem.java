package icy.betterhorses.net.item;

import icy.betterhorses.net.entity.BhUndeadHorse;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.List;

public class UndeadEggItem extends SpawnEggItem {

    private static final RandomSource RANDOM = RandomSource.create();

    private final List<EntityType<BhUndeadHorse>> sizes;

    public UndeadEggItem(List<EntityType<BhUndeadHorse>> sizes, int base, int spots, Properties props) {
        super(sizes.get(1), base, spots, props);
        this.sizes = sizes;
    }

    @Override
    public EntityType<?> getType(ItemStack stack) {
        if (stack.has(DataComponents.ENTITY_DATA)) {
            return super.getType(stack);
        }
        return sizes.get(RANDOM.nextInt(sizes.size()));
    }
}
