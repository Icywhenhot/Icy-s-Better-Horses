package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<BlockEntityType<HitchpostBlockEntity>> HITCHPOST =
            BLOCK_ENTITY_TYPES.register("hitchpost",
                    () -> BlockEntityType.Builder.of(HitchpostBlockEntity::new, ModBlocks.HITCHPOST.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    private ModBlockEntities() {}
}
