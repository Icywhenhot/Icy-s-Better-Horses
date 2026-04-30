package icy.betterhorses.net;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(IcysBetterHorses.registryOwnerId(), Registries.BLOCK);

    public static final RegistrySupplier<Block> HITCHPOST = BLOCKS.register(
            ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"),
            () -> {
                ResourceKey<Block> key = ResourceKey.create(
                        Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "hitchpost"));
                return new HitchpostBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .setId(key));
            });

    /**
     * Called from {@link IcysBetterHorses#init()} before block entity and item setup so
     * that the rest of the registries can reference the registered blocks.
     */
    public static void init() {
        BLOCKS.register();
    }

    private ModBlocks() {}
}
