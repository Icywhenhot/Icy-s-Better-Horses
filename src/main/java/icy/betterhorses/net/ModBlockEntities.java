package icy.betterhorses.net;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(IcysBetterHorses.registryOwnerId(), Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<HitchpostBlockEntity>> HITCHPOST = BLOCK_ENTITIES.register(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"),
            () -> FabricBlockEntityTypeBuilder.create(HitchpostBlockEntity::new, ModBlocks.HITCHPOST.get()).build());

    public static void init() {
        BLOCK_ENTITIES.register();
    }

    private ModBlockEntities() {}
}
