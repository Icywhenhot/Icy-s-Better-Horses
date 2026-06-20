package icy.betterhorses.net.network;

import icy.betterhorses.net.client.RadialMenuScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client-only bodies; safe to load on dedicated servers since the JVM resolves the refs lazily.
public final class ClientPayloadHandlers {

    private ClientPayloadHandlers() {}

    public static void handleOpenRadial(OpenRadialPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.closeContainer();
            }
            client.setScreen(new RadialMenuScreen(payload.horseId()));
        });
    }
}
