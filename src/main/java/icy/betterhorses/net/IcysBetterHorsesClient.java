package icy.betterhorses.net;

import com.mojang.blaze3d.platform.InputConstants;
import icy.betterhorses.net.client.HorseInfoScreen;
import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.OpenRadialPayload;
import icy.betterhorses.net.network.RequestOpenRadialPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = IcysBetterHorses.MOD_ID, dist = Dist.CLIENT)
public final class IcysBetterHorsesClient {

    public static final Logger LOGGER = LoggerFactory.getLogger("icys_better_horses/client");

    public static final KeyMapping CALL_KEY = new KeyMapping(
            "key.icys_better_horses.call",
            InputConstants.Type.KEYSYM,
            80,
            KeyMapping.Category.GAMEPLAY);

    public IcysBetterHorsesClient(IEventBus modEventBus) {
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerClientPayloads);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
        LOGGER.info("[RADIAL][0] Client init complete.");
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CALL_KEY);
    }

    private void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenRadialPayload.TYPE, this::handleOpenRadialPayload);
    }

    private void handleOpenRadialPayload(OpenRadialPayload payload, IPayloadContext context) {
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

    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getTarget() instanceof AbstractHorse horse)) {
            return;
        }
        if (!bh_isControlDown()) {
            return;
        }

        LOGGER.info("[RADIAL][2] Sending RequestOpenRadialPayload(horseId={})", horse.getId());
        ClientPacketDistributor.sendToServer(new RequestOpenRadialPayload(horse.getId()));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        HorseStabilizerSoundController.tick(client);
        if (client.player == null || client.level == null) {
            return;
        }

        while (CALL_KEY.consumeClick()) {
            if (client.player.getVehicle() instanceof AbstractHorse mount) {
                client.setScreen(new HorseInfoScreen(mount));
            } else {
                ClientPacketDistributor.sendToServer(new CallHorsePayload());
            }
        }
    }

    private static boolean bh_isControlDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
