package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModBlocks {

    public static final icy.betterhorses.net.item.HearthlightBlock HEARTHLIGHT =
            new icy.betterhorses.net.item.HearthlightBlock(BlockBehaviour.Properties.of()
                    .noCollission().noOcclusion().replaceable().noLootTable().lightLevel(state -> 10));

    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            helper.register(ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "hearthlight"), HEARTHLIGHT);
        });
    }

    private ModBlocks() {}
}
