package icy.betterhorses.net;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// /horse trust <player>, /horse untrust <player> and /horse trusted: an owner's list of players who
// may ride every horse they own. trust covers riding only, never gear, carts' cargo or disowning
public final class BhCommands {

    private static final String MSG = "message.icys-better-horses.trust.";

    private BhCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> build(dispatcher));
    }

    // no permission gate: a trust list belongs to whoever is running the command, so the only
    // requirement is being a player at all, which getPlayerOrException reports for us
    private static void build(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("horse")
                .then(Commands.literal("trust")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(context -> trust(context, targets(context)))))
                .then(Commands.literal("untrust")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                // suggest from the caller's own list rather than everyone online
                                .suggests((context, builder) -> {
                                    ServerPlayer owner = context.getSource().getPlayer();
                                    return owner == null
                                            ? builder.buildFuture()
                                            : SharedSuggestionProvider.suggest(
                                                    HorseTracker.getTrusted(owner.getUUID()).values(), builder);
                                })
                                .executes(context -> untrust(context, targets(context)))))
                .then(Commands.literal("trusted")
                        .executes(BhCommands::listTrusted)));
    }

    private static Collection<NameAndId> targets(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return GameProfileArgument.getGameProfiles(context, "player");
    }

    private static int trust(CommandContext<CommandSourceStack> context, Collection<NameAndId> profiles)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        int granted = 0;

        for (NameAndId profile : profiles) {
            if (profile.id().equals(owner.getUUID())) {
                source.sendFailure(Component.translatable(MSG + "self"));
                continue;
            }
            if (!HorseTracker.trust(owner.getUUID(), profile.id(), profile.name())) {
                source.sendFailure(Component.translatable(MSG + "already", profile.name()));
                continue;
            }

            granted++;
            source.sendSuccess(() -> Component.translatable(MSG + "added", profile.name())
                    .withStyle(ChatFormatting.GREEN), false);
            notify(source, profile.id(), MSG + "notify_added", owner.nameAndId().name());
            IcysBetterHorses.LOGGER.info("[trust] {} now trusts {} with their horses",
                    owner.nameAndId().name(), profile.name());
        }

        return granted;
    }

    private static int untrust(CommandContext<CommandSourceStack> context, Collection<NameAndId> profiles)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        int revoked = 0;

        for (NameAndId profile : profiles) {
            if (!HorseTracker.untrust(owner.getUUID(), profile.id())) {
                source.sendFailure(Component.translatable(MSG + "not_trusted", profile.name()));
                continue;
            }

            revoked++;
            source.sendSuccess(() -> Component.translatable(MSG + "removed", profile.name())
                    .withStyle(ChatFormatting.YELLOW), false);
            notify(source, profile.id(), MSG + "notify_removed", owner.nameAndId().name());
            IcysBetterHorses.LOGGER.info("[trust] {} no longer trusts {} with their horses",
                    owner.nameAndId().name(), profile.name());

            // a revoked rider still in the saddle is bucked off by the horse's own tick check
        }

        return revoked;
    }

    private static int listTrusted(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();

        Map<UUID, String> trusted = HorseTracker.getTrusted(owner.getUUID());
        if (trusted.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(MSG + "list_empty"), false);
            return 0;
        }

        List<String> names = new ArrayList<>(trusted.values());
        names.sort(String.CASE_INSENSITIVE_ORDER);
        String joined = String.join(", ", names);
        source.sendSuccess(() -> Component.translatable(MSG + "list", trusted.size(), joined), false);
        return trusted.size();
    }

    // tells the other player where they stand, when they're online to hear it
    private static void notify(CommandSourceStack source, UUID targetId, String key, String ownerName) {
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(targetId);
        if (target != null) {
            target.sendSystemMessage(Component.translatable(key, ownerName));
        }
    }
}
