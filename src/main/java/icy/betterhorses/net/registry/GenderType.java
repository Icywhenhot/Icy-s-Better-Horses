package icy.betterhorses.net.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

public final class GenderType {

    public GenderType() {
    }

    public static Component displayName(ResourceKey<GenderType> key) {
        String namespace = key.identifier().getNamespace();
        return Component.translatable("gender." + namespace + "." + key.identifier().getPath());
    }
}
