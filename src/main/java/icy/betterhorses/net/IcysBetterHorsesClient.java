package icy.betterhorses.net;

import com.mojang.blaze3d.platform.InputConstants;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = IcysBetterHorses.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IcysBetterHorsesClient {

    private static boolean tookServerBreeds = false;

    private static final String KEY_CATEGORY = "key.categories.icys-better-horses";
    private static final double RADIAL_REACH = 12.0D;

    public static final KeyMapping CALL_KEY = new KeyMapping(
            "key.icys-better-horses.call",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            KEY_CATEGORY);

    public static final KeyMapping RADIAL_KEY = new KeyMapping(
            "key.icys-better-horses.radial",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY);

    private IcysBetterHorsesClient() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CALL_KEY);
        event.register(RADIAL_KEY);
    }

    public static void bh_tryOpenRadial(Minecraft client) {
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

    public static void receiveConfig(ConfigSyncPayload payload) {
        if (Minecraft.getInstance().hasSingleplayerServer()) return;
        BhConfig.adoptServer(payload.disabledFeatures(), payload.classAbilities(),
                payload.breedAbilities(), payload.disabledAbilities(), payload.tuning());
    }

    public static void receiveBreeds(BreedDataPayload payload) {
        if (Minecraft.getInstance().hasSingleplayerServer()) return;
        BhBreedData.replaceAll(payload.toMap());
        tookServerBreeds = true;
    }

    public static void onDisconnect() {
        BhConfig.dropServer();
        if (tookServerBreeds) {
            BhBreedData.resetToBuiltIn();
            tookServerBreeds = false;
        }
    }
}
