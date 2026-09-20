package icy.betterhorses.net;

import net.minecraft.server.level.ServerPlayer;

/** TODO: port networking to Fabric. Stubbed out so the mod compiles; every method is a no-op. */
public final class BhNetworking {

    private BhNetworking() {}

    // TODO: register server-bound payloads.
    public static void registerServer() {}

    // TODO: register client-bound payloads.
    public static void registerClient() {}

    // TODO: send to the server. No-op for now.
    public static void sendToServer(Object payload) {}

    // TODO: send to a player. No-op for now.
    public static void sendToPlayer(ServerPlayer player, Object payload) {}
}
