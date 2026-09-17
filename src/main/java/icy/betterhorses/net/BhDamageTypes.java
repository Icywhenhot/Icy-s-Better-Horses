package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class BhDamageTypes {

    public static final ResourceKey<DamageType> HORSE_BASH = key("horse_bash");
    public static final ResourceKey<DamageType> HORSE_KICK = key("horse_kick");

    private BhDamageTypes() {}

    public static DamageSource source(Level level, ResourceKey<DamageType> type, Entity direct, Entity causing) {
        return new DamageSource(
                level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type),
                direct, causing);
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, path));
    }
}
