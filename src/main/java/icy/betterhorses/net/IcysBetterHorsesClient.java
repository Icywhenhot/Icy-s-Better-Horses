package icy.betterhorses.net;

import com.mojang.blaze3d.platform.InputConstants;
import icy.betterhorses.net.client.RadialMenuScreen;
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

    private static final String KEY_CATEGORY = "key.categories.icys_better_horses";
    private static final double RADIAL_REACH = 12.0D;

    public static final KeyMapping CALL_KEY = new KeyMapping(
            "key.icys_better_horses.call",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            KEY_CATEGORY);

    public static final KeyMapping RADIAL_KEY = new KeyMapping(
            "key.icys_better_horses.radial",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KEY_CATEGORY);

    private IcysBetterHorsesClient() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CALL_KEY);
        event.register(RADIAL_KEY);
    }

    /** Open the command wheel for the tamed horse the player is looking at, if any. Client-side only. */
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
}
