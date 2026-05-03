package icy.betterhorses.net.platform.neoforge;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class PlatformHelperImpl {
    private PlatformHelperImpl() {}

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        return new BlockEntityType<>(supplier, blocks);
    }
}
