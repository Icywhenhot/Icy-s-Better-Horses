package icy.betterhorses.net.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class PlatformHelper {
    private PlatformHelper() {}

    @ExpectPlatform
    public static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        throw new AssertionError();
    }
}
