package icy.betterhorses.net;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class BhDamageTypes {

    public static final ResourceKey<DamageType> HORSE_BASH = key("horse_bash");
    public static final ResourceKey<DamageType> HORSE_KICK = key("horse_kick");

    private BhDamageTypes() {}

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, path));
    }
}
