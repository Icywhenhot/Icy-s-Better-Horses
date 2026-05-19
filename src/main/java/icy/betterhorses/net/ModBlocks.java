package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IcysBetterHorses.MOD_ID);

    private static final ResourceKey<Block> HITCHPOST_KEY = ResourceKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"));

    public static final Block HITCHPOST = new HitchpostBlock(BlockBehaviour.Properties.of()
            .setId(HITCHPOST_KEY)
            .mapColor(MapColor.WOOD)
            .strength(2.0f, 3.0f)
            .sound(SoundType.WOOD)
            .noOcclusion());

    @SuppressWarnings("unused")
    private static final DeferredHolder<Block, Block> HITCHPOST_ENTRY = BLOCKS.register("hitchpost", () -> HITCHPOST);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    private ModBlocks() {}
}

