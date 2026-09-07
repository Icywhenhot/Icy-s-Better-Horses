package icy.betterhorses.net;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import icy.betterhorses.net.feature.breed.BrickBreak;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

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

    private static int debug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!(player.getVehicle() instanceof AbstractHorse horse)) {
            context.getSource().sendFailure(Component.literal("Sit on the horse first."));
            return 0;
        }
        IHorseData data = IHorseData.of(horse);
        HorseBreed breed = data.bh_getBreed();
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        Vec3 motion = horse.getKnownMovement();
        Vec3 flat = new Vec3(motion.x, 0.0D, motion.z);
        Vec3 stale = horse.getDeltaMovement();
        BhAbility gate = BrickBreak.gate(breed);
        BlockPos ahead = BlockPos.containing(horse.position().add(
                flat.lengthSqr() < 1.0E-4D
                        ? horse.getLookAngle().multiply(1.2D, 0.0D, 1.2D)
                        : flat.normalize().scale(1.2D)));

        say(context, "breed " + breed + ", bond " + data.bh_getBond() + " (tier " + tier + ")");
        say(context, "driver " + (horse.getControllingPassenger() == player ? "you" : "NOT you"));
        say(context, "speed " + String.format("%.3f", flat.length())
                + " / " + BrickBreak.MIN_SPEED + " needed"
                + "  (delta " + String.format("%.3f", Math.sqrt(stale.x * stale.x + stale.z * stale.z)) + ")");
        say(context, "masters: class " + BhConfig.classAbilitiesEnabled()
                + ", breed " + BhConfig.breedAbilitiesEnabled());
        say(context, gate.key() + " = " + gate.on());
        say(context, "any ability for breed = " + BhConfig.anyAbilityEnabled(breed));
        say(context, "block ahead " + horse.level().getBlockState(ahead).getBlock()
                + " breakable=" + BrickBreak.breaks(horse.level().getBlockState(ahead)));
        say(context, "herd gate " + BhAbility.APPALOOSA_HERD.on()
                + ", paused " + data.bh_isAbilityPaused());
        return 1;
    }

    private static void say(CommandContext<CommandSourceStack> context, String line) {
        context.getSource().sendSuccess(() -> Component.literal(line).withStyle(ChatFormatting.GRAY), false);
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> build(dispatcher));
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
                        .executes(BhCommands::listTrusted))
                .then(Commands.literal("debug")
                        .executes(BhCommands::debug)));
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
            double d = horse.distanceToSqr(player);
            if (d < bestDist) {
                bestDist = d;
                best = horse;
            }
        }
        return best;
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
