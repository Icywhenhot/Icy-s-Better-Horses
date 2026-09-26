package icy.betterhorses.net;

import com.mojang.blaze3d.platform.InputConstants;
import icy.betterhorses.net.client.BhClientCaches;
import icy.betterhorses.net.client.CartChestScreen;
import icy.betterhorses.net.client.ChargeShakeController;
import icy.betterhorses.net.client.ClientHorseRoster;
import icy.betterhorses.net.client.ClientTrustCache;
import icy.betterhorses.net.client.HorseGearController;
import icy.betterhorses.net.client.HorseInfoScreen;
import icy.betterhorses.net.client.HorseRosterScreen;
import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.client.render.BelgianHorseRenderer;
import icy.betterhorses.net.client.render.BhClientHorseUnload;
import icy.betterhorses.net.client.render.BhModelLayers;
import icy.betterhorses.net.client.render.ClydesdaleHorseRenderer;
import icy.betterhorses.net.client.render.FriesianHorseRenderer;
import icy.betterhorses.net.client.render.HaflingerHorseRenderer;
import icy.betterhorses.net.client.render.HorseCartRenderer;
import icy.betterhorses.net.client.render.IcelandicHorseRenderer;
import icy.betterhorses.net.client.render.MediumHorseRenderer;
import icy.betterhorses.net.client.render.PercheronHorseRenderer;
import icy.betterhorses.net.client.render.ShireHorseRenderer;
import icy.betterhorses.net.client.render.SmallHorseRenderer;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public final class IcysBetterHorsesClient implements ClientModInitializer {

    private static boolean tookServerBreeds = false;

    private static final String KEY_CATEGORY = "key.categories.icys-better-horses";
    private static final double RADIAL_REACH = 12.0D;

    private static final double REACH = 12.0D;
    private static final double ROUSE_SCAN = 48.0D;

    private static boolean callKeyWasDown = false;

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

    public static final KeyMapping MANAGE_KEY = new KeyMapping(
            "key.icys-better-horses.manage",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY);

    public static final KeyMapping GEAR_KEY = new KeyMapping(
            "key.icys-better-horses.gear",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KEY_CATEGORY);

    public static final KeyMapping REAR_KEY = new KeyMapping(
            "key.icys-better-horses.rear",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KEY_CATEGORY);

    public static final KeyMapping FREE_LOOK_KEY = new KeyMapping(
            "key.icys-better-horses.free_look",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            KEY_CATEGORY);

    public static final KeyMapping CART_SIZE_KEY = new KeyMapping(
            "key.icys-better-horses.cart_size",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            KEY_CATEGORY);

    @Override
    public void onInitializeClient() {
        BhNetworking.registerClient();
        registerKeyMappings();
        registerRenderers();
        BhModelLayers.register();
        MenuScreens.register(ModMenus.CART_CHEST, CartChestScreen::new);
        registerItemColors();
        ClientTickEvents.END_CLIENT_TICK.register(IcysBetterHorsesClient::onClientTick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onDisconnect());
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof AbstractHorse) {
                BhClientHorseUnload.handle(entity.getId());
            }
        });
    }

    private static void registerKeyMappings() {
        KeyBindingHelper.registerKeyBinding(CALL_KEY);
        KeyBindingHelper.registerKeyBinding(RADIAL_KEY);
        KeyBindingHelper.registerKeyBinding(MANAGE_KEY);
        KeyBindingHelper.registerKeyBinding(GEAR_KEY);
        KeyBindingHelper.registerKeyBinding(REAR_KEY);
        KeyBindingHelper.registerKeyBinding(FREE_LOOK_KEY);
        KeyBindingHelper.registerKeyBinding(CART_SIZE_KEY);
    }

    private static void registerItemColors() {
        for (Item egg : ModItems.BREED_SPAWN_EGGS) {
            ColorProviderRegistry.ITEM.register((stack, layer) -> -1, egg);
        }
    }

    private static void registerRenderers() {
        EntityRendererRegistry.register(ModEntities.HORSE_CART, HorseCartRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICELANDIC_HORSE, context ->
                new IcelandicHorseRenderer(context,
                        BhModelLayers.ICELANDIC_HORSE, BhModelLayers.ICELANDIC_HORSE_BABY));
        EntityRendererRegistry.register(ModEntities.FRIESIAN_HORSE, context ->
                new FriesianHorseRenderer(context,
                        BhModelLayers.FRIESIAN_HORSE, BhModelLayers.FRIESIAN_HORSE_BABY));
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
                        BhModelLayers.PERCHERON_HORSE, BhModelLayers.PERCHERON_HORSE_BABY));
        EntityRendererRegistry.register(ModEntities.SHIRE_HORSE, context ->
                new ShireHorseRenderer(context,
                        BhModelLayers.SHIRE_HORSE, BhModelLayers.SHIRE_HORSE_BABY));
        EntityRendererRegistry.register(ModEntities.BELGIAN_HORSE, context ->
                new BelgianHorseRenderer(context,
                        BhModelLayers.BELGIAN_HORSE, BhModelLayers.BELGIAN_HORSE_BABY));
        EntityRendererRegistry.register(ModEntities.CLYDESDALE_HORSE, context ->
                new ClydesdaleHorseRenderer(context,
                        BhModelLayers.CLYDESDALE_HORSE, BhModelLayers.CLYDESDALE_HORSE_BABY));
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
        if (owner != null && !owner.equals(player.getUUID()) && !ClientTrustCache.isTrustedBy(owner)) {
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

    public static void receiveHorseRoster(HorseRosterSyncPayload payload) {
        ClientHorseRoster.setEntries(payload.entries());
    }

    public static void receiveManageResult(HorseManageResultPayload payload) {
        ClientHorseRoster.onActionResult(
                payload.horseId(),
                HorseManageAction.fromId(payload.actionOrdinal()),
                payload.success(),
                payload.messageKey());
    }

    public static void receiveTrust(TrustSyncPayload payload) {
        ClientTrustCache.set(payload.trustingOwners());
    }

    public static void receiveChargeShake(HorseChargeShakePayload payload) {
        ChargeShakeController.trigger();
    }

    private static void onClientTick(Minecraft client) {
        HorseStabilizerSoundController.tick(client);
        if (client.player == null || client.level == null) {
            return;
        }

        boolean callDown = IcysBetterHorsesClient.CALL_KEY.isDown();
        if (callDown && !callKeyWasDown) {
            if (anyHorseRoused(client)) {
                BhNetworking.sendToServer(new HorseRecallPayload());
            } else if (client.player.getVehicle() instanceof AbstractHorse mount) {
                client.setScreen(new HorseInfoScreen(mount));
            } else {
                BhNetworking.sendToServer(new CallHorsePayload());
            }
        }
        callKeyWasDown = callDown;
        while (IcysBetterHorsesClient.CALL_KEY.consumeClick()) {}

        while (IcysBetterHorsesClient.RADIAL_KEY.consumeClick()) {
            IcysBetterHorsesClient.bh_tryOpenRadial(client);
        }

        while (IcysBetterHorsesClient.MANAGE_KEY.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new HorseRosterScreen());
            }
        }

        while (IcysBetterHorsesClient.GEAR_KEY.consumeClick()) {
            shiftGear(client);
        }

        while (IcysBetterHorsesClient.REAR_KEY.consumeClick()) {
            tryRear(client);
        }

        while (IcysBetterHorsesClient.CART_SIZE_KEY.consumeClick()) {
            trySwapCartSize(client);
        }
    }

    private static void shiftGear(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null) {
            return;
        }
        if (!(player.getVehicle() instanceof AbstractHorse horse)
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
        client.gui.setOverlayMessage(Component.translatable("message.icys-better-horses.gait." + gait), false);
    }

    private static void tryRear(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null) {
            return;
        }
        AbstractHorse horse = player.getVehicle() instanceof AbstractHorse mount
                ? mount
                : lookedAtHorse(player);
        if (horse == null) {
            return;
        }
        BhNetworking.sendToServer(new BhRearPayload(horse.getId()));
    }

    private static void trySwapCartSize(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null) {
            return;
        }
        Entity target = lookedAtCartTarget(player);
        if (target == null) {
            return;
        }
        BhNetworking.sendToServer(new CartSizePayload(target.getId()));
    }

    private static @Nullable AbstractHorse lookedAtHorse(LocalPlayer player) {
        Entity hit = lookedAt(player, entity -> entity instanceof AbstractHorse && entity.isPickable());
        return hit instanceof AbstractHorse horse ? horse : null;
    }

    private static @Nullable Entity lookedAtCartTarget(LocalPlayer player) {
        return lookedAt(player, entity ->
                (entity instanceof HorseCartEntity || entity instanceof AbstractHorse) && entity.isPickable());
    }

    private static @Nullable Entity lookedAt(LocalPlayer player, java.util.function.Predicate<Entity> filter) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(REACH));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(REACH)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox, filter, REACH * REACH);
        return hit == null ? null : hit.getEntity();
    }

    private static boolean anyHorseRoused(Minecraft client) {
        UUID self = client.player.getUUID();
        AABB box = client.player.getBoundingBox().inflate(ROUSE_SCAN);
        for (AbstractHorse horse : client.level.getEntitiesOfClass(AbstractHorse.class, box)) {
            IHorseData data = IHorseData.of(horse);
            if (data.bh_getCombatState() != 0 && self.equals(data.bh_getOwner())) {
                return true;
            }
        }
        return false;
    }

    public static void onDisconnect() {
        BhConfig.dropServer();
        if (tookServerBreeds) {
            BhBreedData.resetToBuiltIn();
            tookServerBreeds = false;
        }
        BhClientCaches.resetAll();
    }
}
