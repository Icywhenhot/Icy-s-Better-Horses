package icy.betterhorses.net;

import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.OpenRadialPayload;
import icy.betterhorses.net.network.RequestOpenRadialPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcysBetterHorsesClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("icys-better-horses/client");

    public static KeyMapping CALL_KEY;

    /**
     * 1.21.11: {@link KeyMapping}'s category is now a typed {@link KeyMapping.Category} record,
     * not a free-form translation key. {@link KeyMapping.Category#GAMEPLAY} is the closest
     * built-in match for a "call your horse" bind; we register here on first use.
     */
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.GAMEPLAY;

    @Override
    public void onInitializeClient() {
        CALL_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.call",
                InputConstants.Type.KEYSYM,
                80, // GLFW_KEY_P
                CATEGORY
        ));

        // Server tells us to open the radial menu after a validated Ctrl + right-click on a horse.
        ClientPlayNetworking.registerGlobalReceiver(OpenRadialPayload.TYPE, (payload, context) -> {
            LOGGER.info("[RADIAL][5] S2C received OpenRadialPayload(horseId={})", payload.horseId());
            context.client().execute(() -> {
                if (context.client().player != null) {
                    context.client().player.closeContainer();
                }
                LOGGER.info("[RADIAL][6] Opening RadialMenuScreen for horse {}", payload.horseId());
                context.client().setScreen(new RadialMenuScreen(payload.horseId()));
            });
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // UseEntityCallback fires on both client and server — bail silently on the server.
            if (!world.isClientSide()) {
                return InteractionResult.PASS;
            }
            LOGGER.info("[RADIAL][1] UseEntityCallback fired on client: entity={}, hand={}",
                    entity.getType().toShortString(), hand);
            if (hand != InteractionHand.MAIN_HAND) {
                LOGGER.info("[RADIAL][1a] Skip: hand is {}, need MAIN_HAND", hand);
                return InteractionResult.PASS;
            }
            if (!(entity instanceof AbstractHorse horse)) {
                LOGGER.info("[RADIAL][1b] Skip: entity is not an AbstractHorse (was {})",
                        entity.getClass().getSimpleName());
                return InteractionResult.PASS;
            }
            boolean ctrl = this.bh_isControlDown();
            LOGGER.info("[RADIAL][1c] Target is horse id={}; Ctrl held? {}", horse.getId(), ctrl);
            if (!ctrl) {
                LOGGER.info("[RADIAL][1d] Skip: Ctrl not held, letting vanilla interaction proceed");
                return InteractionResult.PASS;
            }

            LOGGER.info("[RADIAL][2] All gates passed — sending RequestOpenRadialPayload(horseId={})", horse.getId());
            ClientPlayNetworking.send(new RequestOpenRadialPayload(horse.getId()));
            return InteractionResult.CONSUME;
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        LOGGER.info("[RADIAL][0] Client init complete — keybind, OpenRadialPayload receiver, and UseEntityCallback registered");
    }

    private void onClientTick(Minecraft client) {
        HorseStabilizerSoundController.tick(client);
        if (client.player == null || client.level == null) return;

        while (CALL_KEY.consumeClick()) {
            ClientPlayNetworking.send(new CallHorsePayload());
        }
    }

    private boolean bh_isControlDown() {
        // 1.21.11: InputConstants.isKeyDown takes the Window object directly (no longer a long handle).
        com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
}
