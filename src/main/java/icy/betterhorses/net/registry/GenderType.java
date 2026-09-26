package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public final class GenderType {

    public GenderType() {
    }

    public static Component displayName(ResourceKey<GenderType> key) {
        String namespace = key.location().getNamespace().equals(IcysBetterHorses.MOD_ID)
                ? IcysBetterHorses.RESOURCE_NAMESPACE
                : key.location().getNamespace();
        return Component.translatable("gender." + namespace + "." + key.location().getPath());
    }
}
