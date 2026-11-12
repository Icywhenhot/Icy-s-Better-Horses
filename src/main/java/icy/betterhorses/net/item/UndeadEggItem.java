package icy.betterhorses.net.item;

import icy.betterhorses.net.entity.BhUndeadHorse;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class UndeadEggItem extends SpawnEggItem {

    private final List<EntityType<BhUndeadHorse>> sizes;

    public UndeadEggItem(List<EntityType<BhUndeadHorse>> sizes, Properties props) {
        super(props.spawnEgg(sizes.get(1)));
        this.sizes = sizes;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return rolled(ctx.getItemInHand(), ctx.getLevel(), () -> super.useOn(ctx));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return rolled(player.getItemInHand(hand), level, () -> super.use(level, player, hand));
    }

    public EntityType<?> roll(ItemStack stack, RandomSource random) {
        TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
        if (data != null && !data.equals(stack.getPrototype().get(DataComponents.ENTITY_DATA))) {
            return data.type();
        }
        return sizes.get(random.nextInt(sizes.size()));
    }

    private InteractionResult rolled(ItemStack stack, Level level, Supplier<InteractionResult> spawn) {
        TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);
        if (level.isClientSide() || data == null || !data.equals(stack.getPrototype().get(DataComponents.ENTITY_DATA))) {
            return spawn.get();
        }
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(roll(stack, level.getRandom()), new CompoundTag()));
        try {
            return spawn.get();
        } finally {
            stack.set(DataComponents.ENTITY_DATA, data);
        }
    }
}
