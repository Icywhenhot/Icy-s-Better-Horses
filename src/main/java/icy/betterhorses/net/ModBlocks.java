package icy.betterhorses.net;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {

    public static final icy.betterhorses.net.item.HearthlightBlock HEARTHLIGHT =
            Registry.register(BuiltInRegistries.BLOCK,
                    Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hearthlight"),
                    new icy.betterhorses.net.item.HearthlightBlock(BlockBehaviour.Properties.of()
                            .setId(ResourceKey.create(Registries.BLOCK,
                                    Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hearthlight")))
                            .noCollision().noOcclusion().replaceable().noLootTable().lightLevel(state -> 10)));

    public static void init() {
    }

    private ModBlocks() {}
}
