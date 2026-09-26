package icy.betterhorses.net;

import icy.betterhorses.net.item.HearthlightBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

    public static final HearthlightBlock HEARTHLIGHT = Registry.register(
            BuiltInRegistries.BLOCK,
            new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "hearthlight"),
            new HearthlightBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .lightLevel(state -> 10)));

    public static void register() {}

    private ModBlocks() {}
}
