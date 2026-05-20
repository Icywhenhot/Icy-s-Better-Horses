package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public final class ModBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IcysBetterHorses.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HitchpostBlockEntity>> HITCHPOST =
            BLOCK_ENTITY_TYPES.register("hitchpost",
                    () -> new BlockEntityType<>(HitchpostBlockEntity::new, Set.of(ModBlocks.HITCHPOST.get())));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    private ModBlockEntities() {}
}
