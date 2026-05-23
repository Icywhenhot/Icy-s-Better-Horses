package icy.betterhorses.net.network;

import icy.betterhorses.net.client.RadialMenuScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side payload handlers. Lives in common dist but its method bodies are only
 * invoked on the client; the class itself loads safely on dedicated servers because
 * client-only references are confined to method bodies, which the JVM resolves lazily.
 */
public final class ClientPayloadHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger("icys_better_horses/client/payloads");

    private ClientPayloadHandlers() {}

    public static void handleOpenRadial(OpenRadialPayload payload, IPayloadContext context) {
        LOGGER.info("[RADIAL][5] S2C received OpenRadialPayload(horseId={})", payload.horseId());
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.closeContainer();
            }
            LOGGER.info("[RADIAL][6] Opening RadialMenuScreen for horse {}", payload.horseId());
            client.setScreen(new RadialMenuScreen(payload.horseId()));
        });
    }
}
