package icy.betterhorses.net;

import icy.betterhorses.net.client.HorseInfoScreen;
import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.network.CallHorsePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class IcysBetterHorsesClient implements ClientModInitializer {

    // KeyMapping's category is a typed record; register our own so both binds group under "Icy's Better Horses" in Controls (label key: key.category.icys-better-horses.general).
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("icys-better-horses", "general"));
    private static final double RADIAL_REACH = 12.0D;

    public static KeyMapping CALL_KEY;
    public static KeyMapping RADIAL_KEY;

    @Override
    public void onInitializeClient() {
        CALL_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.call",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                CATEGORY));
        RADIAL_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.radial",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft client) {
        HorseStabilizerSoundController.tick(client);
        if (client.player == null || client.level == null) return;

        while (CALL_KEY.consumeClick()) {
            if (client.player.getVehicle() instanceof AbstractHorse mount) {
                client.setScreen(new HorseInfoScreen(mount));
            } else {
                ClientPlayNetworking.send(new CallHorsePayload());
            }
        }

        while (RADIAL_KEY.consumeClick()) {
            bh_tryOpenRadial(client);
        }
    }

    private static void bh_tryOpenRadial(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null) {
            return;
        }
        AbstractHorse horse = bh_lookedAtHorse(player);
        if (horse == null || !horse.isTamed()) {
            return;
        }
        UUID owner = ((IHorseData) horse).bh_getOwner();
        if (owner != null && !owner.equals(player.getUUID())) {
            return;
        }
        client.setScreen(new RadialMenuScreen(horse.getId()));
    }

    private static AbstractHorse bh_lookedAtHorse(LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(RADIAL_REACH));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RADIAL_REACH)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox,
                entity -> entity instanceof AbstractHorse && entity.isPickable(),
                RADIAL_REACH * RADIAL_REACH);
        return hit != null && hit.getEntity() instanceof AbstractHorse horse ? horse : null;
    }
}
