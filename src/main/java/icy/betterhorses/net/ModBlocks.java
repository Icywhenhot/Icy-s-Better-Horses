package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {

    public static final Block HITCHPOST = register("hitchpost",
            new HitchpostBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    /**
     * Called from {@link IcysBetterHorses#onInitialize()} before {@link ModItems#init()} so that
     * the block items can reference the registered blocks.
     */
    public static void init() {
        // Registering happens via static initializer; touching the class triggers it.
    }

    private static Block register(String path, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path),
                block);
    }

    private ModBlocks() {}
}
