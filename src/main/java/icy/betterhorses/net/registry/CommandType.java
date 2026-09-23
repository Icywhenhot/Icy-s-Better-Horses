package icy.betterhorses.net.registry;

import icy.betterhorses.net.IcysBetterHorses;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class CommandType {

    private final BiPredicate<AbstractHorse, Player> available;
    private final BiConsumer<AbstractHorse, ServerPlayer> action;

    public CommandType() {
        this.available = (horse, player) -> true;
        this.action = null;
    }

    public CommandType(BiPredicate<AbstractHorse, Player> available,
                       BiConsumer<AbstractHorse, ServerPlayer> action) {
        this.available = Objects.requireNonNull(available);
        this.action = Objects.requireNonNull(action);
    }

    public boolean custom() {
        return action != null;
    }

    public boolean available(AbstractHorse horse, Player player) {
        return available.test(horse, player);
    }

    public boolean execute(AbstractHorse horse, ServerPlayer player) {
        if (action == null || !available(horse, player)) return false;
        action.accept(horse, player);
        return true;
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
