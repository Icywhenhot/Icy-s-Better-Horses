package icy.betterhorses.net;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import icy.betterhorses.net.item.HitchpostBlockEntity;
import icy.betterhorses.net.platform.PlatformHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(IcysBetterHorses.registryOwnerId(), Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<HitchpostBlockEntity>> HITCHPOST = BLOCK_ENTITIES.register(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"),
            () -> PlatformHelper.createBlockEntityType(HitchpostBlockEntity::new, ModBlocks.HITCHPOST.get()));

    public static void init() {
        BLOCK_ENTITIES.register();
    }

    private ModBlockEntities() {}
}
