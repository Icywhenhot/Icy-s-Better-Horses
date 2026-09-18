package icy.betterhorses.net;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BhCommands {

    private static final String MSG = "message.icys-better-horses.trust.";
    private static final String BOND_MSG = "message.icys-better-horses.bond.";
    private static final int BOND_MAX = 100;
    private static final double BOND_REACH = 8.0D;

    private BhCommands() {}

    public static void register(RegisterCommandsEvent event) {
        build(event.getDispatcher());
    }

    private static void build(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bond")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, BOND_MAX))
                        .executes(context -> setBond(context,
                                IntegerArgumentType.getInteger(context, "level")))));

        dispatcher.register(Commands.literal("horse")
                .then(Commands.literal("trust")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(context -> trust(context, targets(context)))))
                .then(Commands.literal("untrust")
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
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

    private static int setBond(CommandContext<CommandSourceStack> context, int level)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        if (!player.isCreative()) {
            source.sendFailure(Component.translatable(BOND_MSG + "creative_only"));
            return 0;
        }

        AbstractHorse horse = player.getVehicle() instanceof AbstractHorse mount ? mount : nearby(player);
        if (horse == null) {
            source.sendFailure(Component.translatable(BOND_MSG + "no_horse"));
            return 0;
        }

        IHorseData data = IHorseData.of(horse);
        if (!player.getUUID().equals(data.bh_getOwner())) {
            source.sendFailure(Component.translatable(BOND_MSG + "not_owner"));
            return 0;
        }

        data.bh_setBond(level);
        int tier = BhHorseTraits.bondTier(level);
        source.sendSuccess(() -> Component.translatable(BOND_MSG + "set",
                horse.getDisplayName(), level, tier + 1).withStyle(ChatFormatting.GREEN), false);
        return level;
    }

    private static AbstractHorse nearby(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(BOND_REACH);
        AbstractHorse best = null;
        double bestDist = Double.MAX_VALUE;
        for (AbstractHorse horse : player.level().getEntitiesOfClass(AbstractHorse.class, box)) {
            if (!player.getUUID().equals(IHorseData.of(horse).bh_getOwner())) continue;
            double d = horse.distanceToSqr(player);
            if (d < bestDist) {
                bestDist = d;
                best = horse;
            }
        }
        return best;
    }

    private static Collection<GameProfile> targets(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return GameProfileArgument.getGameProfiles(context, "player");
    }

    private static int trust(CommandContext<CommandSourceStack> context, Collection<GameProfile> profiles)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        int granted = 0;

        for (GameProfile profile : profiles) {
            if (profile.getId().equals(owner.getUUID())) {
                source.sendFailure(Component.translatable(MSG + "self"));
                continue;
            }
            if (!HorseTracker.trust(owner.getUUID(), profile.getId(), profile.getName())) {
                source.sendFailure(Component.translatable(MSG + "already", profile.getName()));
                continue;
            }

            granted++;
            source.sendSuccess(() -> Component.translatable(MSG + "added", profile.getName())
                    .withStyle(ChatFormatting.GREEN), false);
            notify(source, profile.getId(), MSG + "notify_added", owner.getGameProfile().getName());
            IcysBetterHorses.LOGGER.info("[trust] {} now trusts {} with their horses",
                    owner.getGameProfile().getName(), profile.getName());
        }

        return granted;
    }

    private static int untrust(CommandContext<CommandSourceStack> context, Collection<GameProfile> profiles)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer owner = source.getPlayerOrException();
        int revoked = 0;

        for (GameProfile profile : profiles) {
            if (!HorseTracker.untrust(owner.getUUID(), profile.getId())) {
                source.sendFailure(Component.translatable(MSG + "not_trusted", profile.getName()));
                continue;
            }

            revoked++;
            source.sendSuccess(() -> Component.translatable(MSG + "removed", profile.getName())
                    .withStyle(ChatFormatting.YELLOW), false);
            notify(source, profile.getId(), MSG + "notify_removed", owner.getGameProfile().getName());
            IcysBetterHorses.LOGGER.info("[trust] {} no longer trusts {} with their horses",
                    owner.getGameProfile().getName(), profile.getName());

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

    private static void notify(CommandSourceStack source, UUID targetId, String key, String ownerName) {
        ServerPlayer target = source.getServer().getPlayerList().getPlayer(targetId);
        if (target != null) {
            target.sendSystemMessage(Component.translatable(key, ownerName));
            IcysBetterHorses.sendTrustList(target);
        }
    }
}
