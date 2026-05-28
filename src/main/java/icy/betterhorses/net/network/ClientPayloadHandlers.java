package icy.betterhorses.net.network;

import icy.betterhorses.net.client.RadialMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Client-side payload handlers. The method bodies are only invoked on the client;
 * the class itself stays common-safe because the client-only references are inside
 * the handler body.
 */
public final class ClientPayloadHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger("icys_better_horses/client/payloads");

    private ClientPayloadHandlers() {}

    public static void handleOpenRadial(OpenRadialPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        LOGGER.info("[RADIAL][5] S2C received OpenRadialPayload(horseId={})", payload.horseId());
        context.enqueueWork(() -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                client.player.closeContainer();
            }
            LOGGER.info("[RADIAL][6] Opening RadialMenuScreen for horse {}", payload.horseId());
            client.setScreen(new RadialMenuScreen(payload.horseId()));
        });
        context.setPacketHandled(true);
    }
}
