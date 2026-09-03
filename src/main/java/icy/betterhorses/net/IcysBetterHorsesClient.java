package icy.betterhorses.net;

import icy.betterhorses.net.client.ClientHorseRoster;
import icy.betterhorses.net.client.ClientTrustCache;
import icy.betterhorses.net.client.HorseGearController;
import icy.betterhorses.net.client.HorseInfoScreen;
import icy.betterhorses.net.client.HorseRosterScreen;
import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.client.render.BhModelLayers;
import icy.betterhorses.net.client.render.FriesianHorseRenderer;
import icy.betterhorses.net.client.render.PercheronHorseRenderer;
import icy.betterhorses.net.client.render.BelgianHorseRenderer;
import icy.betterhorses.net.client.render.ClydesdaleHorseRenderer;
import icy.betterhorses.net.client.render.ShireHorseRenderer;
import icy.betterhorses.net.client.render.HorseCartRenderer;
import icy.betterhorses.net.client.render.HaflingerHorseRenderer;
import icy.betterhorses.net.client.render.IcelandicHorseRenderer;
import icy.betterhorses.net.client.render.MediumHorseRenderer;
import icy.betterhorses.net.client.render.SmallHorseRenderer;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.ModMenus;
import icy.betterhorses.net.client.CartChestScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;
import com.klikli_dev.modonomicon.client.render.page.PageRendererRegistry;
import icy.betterhorses.net.book.BhBreedCoatsPage;
import icy.betterhorses.net.client.BhClientCaches;
import icy.betterhorses.net.client.book.BhBreedCoatsPageRenderer;

public class IcysBetterHorsesClient implements ClientModInitializer {

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("icys-better-horses", "general"));
    private static final double RADIAL_REACH = 12.0D;

    public static KeyMapping CALL_KEY;
    public static KeyMapping RADIAL_KEY;
    public static KeyMapping MANAGE_KEY;
    public static KeyMapping GEAR_KEY;
    public static KeyMapping REAR_KEY;
    public static KeyMapping FREE_LOOK_KEY;
    public static KeyMapping CART_SIZE_KEY;

    private static final double BH_ROUSE_SCAN = 32.0D;

    private boolean callKeyWasDown = false;

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
        MANAGE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.manage",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY));

        GEAR_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.gear",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY));

        REAR_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.rear",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY));

        FREE_LOOK_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.free_look",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                CATEGORY));

        CART_SIZE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.icys-better-horses.cart_size",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY));

        EntityRendererRegistry.register(ModEntities.HORSE_CART, HorseCartRenderer::new);
        MenuScreens.register(ModMenus.CART_CHEST, CartChestScreen::new);

        BhModelLayers.register();
        EntityRendererRegistry.register(ModEntities.ICELANDIC_HORSE, context ->
                new IcelandicHorseRenderer(context,
                        BhModelLayers.ICELANDIC_HORSE,
                        BhModelLayers.ICELANDIC_HORSE_BABY));
        EntityRendererRegistry.register(ModEntities.FRIESIAN_HORSE, context ->
                new FriesianHorseRenderer(context,
                        BhModelLayers.FRIESIAN_HORSE,
                        BhModelLayers.FRIESIAN_HORSE_BABY));

        EntityRendererRegistry.register(ModEntities.HAFLINGER_HORSE, HaflingerHorseRenderer::new);

        EntityRendererRegistry.register(ModEntities.APPALOOSA_HORSE, MediumHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.THOROUGHBRED_HORSE, MediumHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.AMERICAN_PAINT_HORSE, MediumHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.ANDALUSIAN_HORSE, MediumHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.MUSTANG_HORSE, MediumHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.QUARTER_HORSE, MediumHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.ARABIAN_HORSE, SmallHorseRenderer::new);
        EntityRendererRegistry.register(ModEntities.MORGAN_HORSE, SmallHorseRenderer::new);

        EntityRendererRegistry.register(ModEntities.PERCHERON_HORSE, context ->
                new PercheronHorseRenderer(context,
                        BhModelLayers.PERCHERON_HORSE,
                        BhModelLayers.PERCHERON_HORSE_BABY));

        EntityRendererRegistry.register(ModEntities.SHIRE_HORSE, context ->
                new ShireHorseRenderer(context,
                        BhModelLayers.SHIRE_HORSE,
                        BhModelLayers.SHIRE_HORSE_BABY));

        EntityRendererRegistry.register(ModEntities.BELGIAN_HORSE, context ->
                new BelgianHorseRenderer(context,
                        BhModelLayers.BELGIAN_HORSE,
                        BhModelLayers.BELGIAN_HORSE_BABY));

        EntityRendererRegistry.register(ModEntities.CLYDESDALE_HORSE, context ->
                new ClydesdaleHorseRenderer(context,
                        BhModelLayers.CLYDESDALE_HORSE,
                        BhModelLayers.CLYDESDALE_HORSE_BABY));

        PageRendererRegistry.registerPageRenderer(
                BhBreedCoatsPage.ID,
                page -> new BhBreedCoatsPageRenderer(
                        (BhBreedCoatsPage) page));

        registerClientHandlers();
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(HorseRosterSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientHorseRoster.setEntries(payload.entries())));

        ClientPlayNetworking.registerGlobalReceiver(HorseManageResultPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientHorseRoster.onActionResult(
                        payload.horseId(),
                        HorseManageAction.fromId(payload.actionOrdinal()),
                        payload.success(),
                        payload.messageKey())));

        ClientPlayNetworking.registerGlobalReceiver(TrustSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientTrustCache.set(payload.trustingOwners())));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                BhClientCaches.resetAll());
    }

    private void onClientTick(Minecraft client) {
        HorseStabilizerSoundController.tick(client);
        if (client.player == null || client.level == null) return;

        boolean callKeyDown = CALL_KEY.isDown();
        if (callKeyDown && !callKeyWasDown) {
            if (bh_anyHorseRoused(client)) {
                ClientPlayNetworking.send(new HorseRecallPayload());
            } else if (client.player.getVehicle() instanceof AbstractHorse mount) {
                client.setScreenAndShow(new HorseInfoScreen(mount));
            } else {
                ClientPlayNetworking.send(new CallHorsePayload());
            }
        }
        callKeyWasDown = callKeyDown;
        while (CALL_KEY.consumeClick()) {}

        while (RADIAL_KEY.consumeClick()) {
            bh_tryOpenRadial(client);
        }

        while (MANAGE_KEY.consumeClick()) {
            if (client.gui.screen() == null) {
                client.setScreenAndShow(new HorseRosterScreen());
            }
        }

        while (GEAR_KEY.consumeClick()) {
            bh_shiftGear(client);
        }

        while (REAR_KEY.consumeClick()) {
            bh_tryRear(client);
        }

        while (CART_SIZE_KEY.consumeClick()) {
            bh_trySwapCartSize(client);
        }
    }

    private static void bh_shiftGear(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.gui.screen() != null) {
            return;
        }
        if (!(player.getControlledVehicle() instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player) {
            return;
        }

        int gear = HorseGearController.INSTANCE.shiftUp(horse);
        String gait = switch (gear) {
            case BhGears.WALK_GEAR -> "walk";
            case BhGears.TROT_GEAR -> horse instanceof IcelandicHorse ? "tolt" : "trot";
            case BhGears.CANTER_GEAR -> "canter";
            case BhGears.GALLOP_GEAR -> "gallop";
            default -> "halt";
        };
        client.gui.chatListener().handleOverlay(
                Component.translatable("message.icys-better-horses.gait." + gait));
    }

    private static void bh_tryRear(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.gui.screen() != null) {
            return;
        }

        AbstractHorse horse = (player.getControlledVehicle() instanceof AbstractHorse mount)
                ? mount
                : bh_lookedAtHorse(player);
        if (horse == null) {
            return;
        }

        ClientPlayNetworking.send(new BhRearPayload(horse.getId()));
    }

    private static void bh_trySwapCartSize(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.gui.screen() != null) {
            return;
        }

        Entity target = bh_lookedAtCartTarget(player);
        if (target == null) {
            return;
        }

        ClientPlayNetworking.send(new CartSizePayload(target.getId()));
    }

    private static @Nullable Entity bh_lookedAtCartTarget(LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(RADIAL_REACH));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RADIAL_REACH)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox,
                entity -> (entity instanceof HorseCartEntity || entity instanceof AbstractHorse)
                        && entity.isPickable(),
                RADIAL_REACH * RADIAL_REACH);
        return hit == null ? null : hit.getEntity();
    }

    private static boolean bh_anyHorseRoused(Minecraft client) {
        UUID self = client.player.getUUID();
        AABB box = client.player.getBoundingBox().inflate(BH_ROUSE_SCAN);
        for (AbstractHorse horse : client.level.getEntitiesOfClass(AbstractHorse.class, box)) {
            IHorseData data = IHorseData.of(horse);
            if (data.bh_getCombatState() != 0 && self.equals(data.bh_getOwner())) {
                return true;
            }
        }
        return false;
    }

    private static void bh_tryOpenRadial(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.gui.screen() != null) {
            return;
        }
        AbstractHorse horse = bh_lookedAtHorse(player);
        if (horse == null || !horse.isTamed()) {
            return;
        }
        UUID owner = IHorseData.of(horse).bh_getOwner();
        if (owner != null
                && !owner.equals(player.getUUID())
                && !ClientTrustCache.isTrustedBy(owner)) {
            return;
        }
        client.setScreenAndShow(new RadialMenuScreen(horse.getId(),
                client.player.getVehicle() instanceof AbstractHorse));
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
