package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    /**
     * 1.21.11 made {@link BlockEntityType}'s constructor and the inner {@code BlockEntitySupplier}
     * package-private. Fabric API exposes {@link FabricBlockEntityTypeBuilder} as the supported
     * way to create one — same outcome, public API.
     */
    public static final BlockEntityType<HitchpostBlockEntity> HITCHPOST = register(
            "hitchpost",
            FabricBlockEntityTypeBuilder.create(HitchpostBlockEntity::new, ModBlocks.HITCHPOST).build());

    public static void init() {
        // Registration happens via static initializer; touching the class triggers it.
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType<T> type) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                type);
    }

    private ModBlockEntities() {}
}
