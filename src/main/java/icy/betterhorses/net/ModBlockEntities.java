package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.Constructor;
import java.util.Set;

public final class ModBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IcysBetterHorses.MOD_ID);

    public static final BlockEntityType<HitchpostBlockEntity> HITCHPOST = create(HitchpostBlockEntity::new, ModBlocks.HITCHPOST);

    @SuppressWarnings("unused")
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HitchpostBlockEntity>> HITCHPOST_ENTRY = BLOCK_ENTITY_TYPES.register("hitchpost", () -> HITCHPOST);

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> create(BlockEntityType.BlockEntitySupplier<? extends T> supplier, Block... validBlocks) {
        try {
            Constructor<BlockEntityType> ctor = BlockEntityType.class.getDeclaredConstructor(BlockEntityType.BlockEntitySupplier.class, Set.class);
            ctor.setAccessible(true);
            return (BlockEntityType<T>) ctor.newInstance(supplier, Set.of(validBlocks));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to construct block entity type", exception);
        }
    }

    private ModBlockEntities() {}
}
