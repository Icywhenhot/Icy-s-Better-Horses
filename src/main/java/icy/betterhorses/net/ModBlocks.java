package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IcysBetterHorses.MOD_ID);

    public static final RegistryObject<HitchpostBlock> HITCHPOST = BLOCKS.register("hitchpost", () ->
            new HitchpostBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    private ModBlocks() {}
}

