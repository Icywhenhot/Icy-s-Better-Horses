package icy.betterhorses.net;

import com.mojang.blaze3d.platform.InputConstants;
import icy.betterhorses.net.client.BhClientCaches;
import icy.betterhorses.net.client.CartChestScreen;
import icy.betterhorses.net.client.ChargeShakeController;
import icy.betterhorses.net.client.ClientHorseRoster;
import icy.betterhorses.net.client.ClientTrustCache;
import icy.betterhorses.net.client.RadialMenuScreen;
import icy.betterhorses.net.client.render.BelgianHorseRenderer;
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
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import icy.betterhorses.net.client.BhConfigScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
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

    private IcysBetterHorsesClient() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CALL_KEY);
        event.register(RADIAL_KEY);
        event.register(MANAGE_KEY);
        event.register(GEAR_KEY);
        event.register(REAR_KEY);
        event.register(FREE_LOOK_KEY);
        event.register(CART_SIZE_KEY);
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

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HORSE_CART.get(), HorseCartRenderer::new);
        event.registerEntityRenderer(ModEntities.ICELANDIC_HORSE.get(), context ->
                new IcelandicHorseRenderer(context,
                        BhModelLayers.ICELANDIC_HORSE, BhModelLayers.ICELANDIC_HORSE_BABY));
        event.registerEntityRenderer(ModEntities.FRIESIAN_HORSE.get(), context ->
                new FriesianHorseRenderer(context,
                        BhModelLayers.FRIESIAN_HORSE, BhModelLayers.FRIESIAN_HORSE_BABY));
        event.registerEntityRenderer(ModEntities.HAFLINGER_HORSE.get(), HaflingerHorseRenderer::new);

        event.registerEntityRenderer(ModEntities.APPALOOSA_HORSE.get(), MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.THOROUGHBRED_HORSE.get(), MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.AMERICAN_PAINT_HORSE.get(), MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.ANDALUSIAN_HORSE.get(), MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSTANG_HORSE.get(), MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.QUARTER_HORSE.get(), MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.ARABIAN_HORSE.get(), SmallHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.MORGAN_HORSE.get(), SmallHorseRenderer::new);

        event.registerEntityRenderer(ModEntities.PERCHERON_HORSE.get(), context ->
                new PercheronHorseRenderer(context,
                        BhModelLayers.PERCHERON_HORSE, BhModelLayers.PERCHERON_HORSE_BABY));
        event.registerEntityRenderer(ModEntities.SHIRE_HORSE.get(), context ->
                new ShireHorseRenderer(context,
                        BhModelLayers.SHIRE_HORSE, BhModelLayers.SHIRE_HORSE_BABY));
        event.registerEntityRenderer(ModEntities.BELGIAN_HORSE.get(), context ->
                new BelgianHorseRenderer(context,
                        BhModelLayers.BELGIAN_HORSE, BhModelLayers.BELGIAN_HORSE_BABY));
        event.registerEntityRenderer(ModEntities.CLYDESDALE_HORSE.get(), context ->
                new ClydesdaleHorseRenderer(context,
                        BhModelLayers.CLYDESDALE_HORSE, BhModelLayers.CLYDESDALE_HORSE_BABY));
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        BhModelLayers.register(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.CART_CHEST.get(), CartChestScreen::new);
            if (ModList.get().isLoaded("cloth_config")) {
                ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, parent) -> BhConfigScreen.create(parent)));
            }
        });
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (RegistryObject<Item> egg : ModItems.BREED_SPAWN_EGGS) {
            event.register((stack, layer) -> -1, egg.get());
        }
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
