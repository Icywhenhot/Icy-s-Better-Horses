package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final BlockEntityType<HitchpostBlockEntity> HITCHPOST = register(
            "hitchpost",
            FabricBlockEntityTypeBuilder.create(HitchpostBlockEntity::new, ModBlocks.HITCHPOST).build());

    public static void init() {
        // Registration happens via static initializer; touching the class triggers it.
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType<T> type) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                type);
    }

    private ModBlockEntities() {}
}
