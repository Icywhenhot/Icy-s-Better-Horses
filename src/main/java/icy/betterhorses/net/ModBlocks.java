package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModBlocks {

    public static final icy.betterhorses.net.item.HearthlightBlock HEARTHLIGHT =
            new icy.betterhorses.net.item.HearthlightBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK,
                            Identifier.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "hearthlight")))
                    .noCollision().noOcclusion().replaceable().noLootTable().lightLevel(state -> 10));

    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            helper.register(Identifier.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "hearthlight"), HEARTHLIGHT);
        });
    }

    private ModBlocks() {}
}
