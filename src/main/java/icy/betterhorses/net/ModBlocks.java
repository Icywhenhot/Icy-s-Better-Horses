package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class ModBlocks {

    public static final icy.betterhorses.net.item.HearthlightBlock HEARTHLIGHT =
            new icy.betterhorses.net.item.HearthlightBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK,
                            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hearthlight")))
                    .noCollision().noOcclusion().replaceable().noLootTable().lightLevel(state -> 10));

    private static final ResourceKey<Block> HITCHPOST_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"));

    public static final Block HITCHPOST = new HitchpostBlock(BlockBehaviour.Properties.of()
                    .setId(HITCHPOST_KEY)
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, helper -> {
            helper.register(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hearthlight"), HEARTHLIGHT);
            helper.register(Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"), HITCHPOST);
        });
    }

    private ModBlocks() {}
}
