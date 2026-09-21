package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import java.util.Objects;

public final class CommandType {

    public CommandType() {
    }

    public static Component displayName(ResourceKey<CommandType> key) {
        String namespace = key.location().getNamespace().equals(IcysBetterHorses.MOD_ID)
                ? IcysBetterHorses.RESOURCE_NAMESPACE
                : key.location().getNamespace();
        return Component.translatable("command." + namespace + "." + key.location().getPath());
    }

    public static boolean toggleable(ResourceKey<BreedType> breedKey) {
        return Objects.equals(breedKey, BhContent.APPALOOSA.getKey());
    }
}
