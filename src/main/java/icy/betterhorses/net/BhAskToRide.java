package icy.betterhorses.net;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

// Chat-based ask-to-ride flow: fired when a real player is refused mounting someone else's horse.
public final class BhAskToRide {

    private static final String MSG = "message.icys-better-horses.ask_ride.";
    private static final String NOT_OWNER_KEY = "message.icys-better-horses.not_owner";

    private BhAskToRide() {}

    public static void handleRefusal(ServerPlayer requester, UUID ownerId) {
        MinecraftServer server = requester.getServer();
        ServerPlayer owner = server == null ? null : server.getPlayerList().getPlayer(ownerId);
        if (owner == null) {
            requester.sendSystemMessage(
                    Component.translatable(MSG + "owner_offline", ownerDisplayName(server, ownerId)));
            return;
        }

        BhRideRequests.Outcome outcome = BhRideRequests.instance().request(ownerId, requester.getUUID());
        switch (outcome) {
            case MUTED -> requester.sendSystemMessage(Component.translatable(NOT_OWNER_KEY));
            case COOLDOWN -> requester.sendSystemMessage(
                    Component.translatable(MSG + "cooldown", owner.getGameProfile().getName()));
            case SEND -> {
                owner.sendSystemMessage(askComponent(requester.getGameProfile().getName()));
                requester.sendSystemMessage(Component.translatable(MSG + "sent", owner.getGameProfile().getName()));
            }
        }
    }

    private static MutableComponent askComponent(String requesterName) {
        MutableComponent allow = Component.translatable(MSG + "allow")
                .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/horse trust " + requesterName))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable(MSG + "allow_hover", requesterName))));
        MutableComponent deny = Component.translatable(MSG + "deny")
                .withStyle(style -> style.withColor(ChatFormatting.RED)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/horse deny " + requesterName))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable(MSG + "deny_hover", requesterName))));
        return Component.translatable(MSG + "request", requesterName)
                .append(allow)
                .append(Component.literal(" "))
                .append(deny);
    }

    private static String ownerDisplayName(@Nullable MinecraftServer server, UUID ownerId) {
        if (server == null || server.getProfileCache() == null) {
            return ownerId.toString();
        }
        return server.getProfileCache().get(ownerId).map(GameProfile::getName).orElse(ownerId.toString());
    }
}
