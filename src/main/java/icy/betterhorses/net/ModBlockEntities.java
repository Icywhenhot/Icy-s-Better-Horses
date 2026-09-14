package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Set;

public final class ModBlockEntities {

    public static final BlockEntityType<HitchpostBlockEntity> HITCHPOST =
            new BlockEntityType<>(HitchpostBlockEntity::new, Set.of(ModBlocks.HITCHPOST), null);

    public static void register(RegisterEvent event) {
        event.register(
                Registries.BLOCK_ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(IcysBetterHorses.RESOURCE_NAMESPACE, "hitchpost"),
                () -> HITCHPOST);
    }

    private ModBlockEntities() {}
}
