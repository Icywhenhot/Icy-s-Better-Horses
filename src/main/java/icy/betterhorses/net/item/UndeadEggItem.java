package icy.betterhorses.net.item;

import icy.betterhorses.net.entity.BhUndeadHorse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UndeadEggItem extends BhSpawnEggItem {

    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    private final List<RegistryObject<EntityType<BhUndeadHorse>>> sizes;

    public UndeadEggItem(List<RegistryObject<EntityType<BhUndeadHorse>>> sizes, int base, int spots, Properties props) {
        super(sizes.get(1), base, spots, props);
        this.sizes = sizes;
    }

    @Override
    public EntityType<?> getType(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("EntityTag")) {
            return super.getType(tag);
        }
        return sizes.get(RANDOM.nextInt(sizes.size())).get();
    }
}
