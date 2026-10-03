package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModBlocks {

    public static final icy.betterhorses.net.item.HearthlightBlock HEARTHLIGHT =
            new icy.betterhorses.net.item.HearthlightBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().replaceable().noLootTable().lightLevel(state -> 10));

    public static void init() {
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hearthlight"), HEARTHLIGHT);
    }

    private ModBlocks() {}
}
