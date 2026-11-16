package icy.betterhorses.net;

import net.minecraft.world.item.Items;
import java.util.List;

import icy.betterhorses.net.client.render.BhJumpClips;
import icy.betterhorses.net.client.render.BhBreedCarpets;
import icy.betterhorses.net.client.render.BhNamedCoats;
import icy.betterhorses.net.client.render.BhTackTextures;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import com.mojang.blaze3d.platform.InputConstants;
import icy.betterhorses.net.client.BhClientCaches;
import icy.betterhorses.net.client.CartScreen;
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
import icy.betterhorses.net.client.render.DonkeyRenderer;
import icy.betterhorses.net.client.render.LargeDonkeyModel;
import icy.betterhorses.net.client.render.MiniatureDonkeyModel;
import icy.betterhorses.net.client.render.UndeadHorseRenderer;
import icy.betterhorses.net.client.render.MuleRenderer;
import icy.betterhorses.net.client.render.IcelandicHorseRenderer;
import icy.betterhorses.net.client.render.MediumHorseRenderer;
import icy.betterhorses.net.client.render.PercheronHorseRenderer;
import icy.betterhorses.net.client.render.ShireHorseRenderer;
import icy.betterhorses.net.client.render.SmallHorseRenderer;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseJumpPayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import icy.betterhorses.net.client.BhConfigScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.BhChargePayload;
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
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
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

    public static final KeyMapping GEAR_DOWN_KEY = new KeyMapping(
            "key.icys-better-horses.gear_down",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
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

    public static final KeyMapping CART_MENU_KEY = new KeyMapping(
            "key.icys-better-horses.cart_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            KEY_CATEGORY);

    public static final KeyMapping CHARGE_KEY = new KeyMapping(
            "key.icys-better-horses.charge",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            KEY_CATEGORY);

    public static final KeyMapping RIDER_PANEL_KEY = new KeyMapping(
            "key.icys-better-horses.rider_panel",
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            KEY_CATEGORY);

    private IcysBetterHorsesClient() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CALL_KEY);
        event.register(RADIAL_KEY);
        event.register(MANAGE_KEY);
        event.register(GEAR_KEY);
        event.register(GEAR_DOWN_KEY);
        event.register(REAR_KEY);
        event.register(FREE_LOOK_KEY);
        event.register(CART_MENU_KEY);
        event.register(CHARGE_KEY);
        event.register(RIDER_PANEL_KEY);
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
        if (owner != null && !owner.equals(player.getUUID())
                && !ClientTrustCache.isTrustedBy(owner)) {
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
                entity -> BhHorseKind.managed(entity) && entity.isPickable(),
                RADIAL_REACH * RADIAL_REACH);
        return hit != null && hit.getEntity() instanceof AbstractHorse horse ? horse : null;
    }

    public static void receiveConfig(ConfigSyncPayload payload) {
        if (Minecraft.getInstance().hasSingleplayerServer()) return;
        BhConfig.adoptServer(payload.disabledFeatures(), payload.classAbilities(),
                payload.breedAbilities(), payload.disabledAbilities(), payload.tuning(), payload.breedingFoods());
    }

    public static void receiveBreeds(BreedDataPayload payload) {
        if (Minecraft.getInstance().hasSingleplayerServer()) return;
        BhBreedData.replaceAll(payload.toMap());
        payload.applyClassSpeeds();
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

    public static void receiveJump(HorseJumpPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null
                && client.level.getEntity(payload.horseId()) instanceof AbstractHorse horse
                && horse.getControllingPassenger() != client.player) {
            IHorseData.of(horse).bh_cueJump();
        }
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LARGE_SKELETON_HORSE.get(), context -> new UndeadHorseRenderer(context,
                BhModelLayers.LARGE_UNDEAD_HORSE, BhModelLayers.PERCHERON_SADDLE, BhModelLayers.PERCHERON_CARPET,
                BhModelLayers.PERCHERON_ARMOR, BhModelLayers.PERCHERON_CHEST, BhTackTextures.PERCHERON));
        event.registerEntityRenderer(ModEntities.MEDIUM_SKELETON_HORSE.get(), context -> new UndeadHorseRenderer(context,
                BhModelLayers.MEDIUM_UNDEAD_HORSE, BhModelLayers.MEDIUM_SADDLE, BhModelLayers.MEDIUM_CARPET,
                BhModelLayers.MEDIUM_ARMOR, BhModelLayers.MEDIUM_CHEST, BhTackTextures.MEDIUM));
        event.registerEntityRenderer(ModEntities.SMALL_SKELETON_HORSE.get(), context -> new UndeadHorseRenderer(context,
                BhModelLayers.SMALL_UNDEAD_HORSE, BhModelLayers.SMALL_SADDLE, BhModelLayers.SMALL_CARPET,
                BhModelLayers.SMALL_ARMOR, BhModelLayers.SMALL_CHEST, BhTackTextures.SMALL));
        event.registerEntityRenderer(ModEntities.LARGE_ZOMBIE_HORSE.get(), context -> new UndeadHorseRenderer(context,
                BhModelLayers.LARGE_UNDEAD_HORSE, BhModelLayers.PERCHERON_SADDLE, BhModelLayers.PERCHERON_CARPET,
                BhModelLayers.PERCHERON_ARMOR, BhModelLayers.PERCHERON_CHEST, BhTackTextures.PERCHERON));
        event.registerEntityRenderer(ModEntities.MEDIUM_ZOMBIE_HORSE.get(), context -> new UndeadHorseRenderer(context,
                BhModelLayers.MEDIUM_UNDEAD_HORSE, BhModelLayers.MEDIUM_SADDLE, BhModelLayers.MEDIUM_CARPET,
                BhModelLayers.MEDIUM_ARMOR, BhModelLayers.MEDIUM_CHEST, BhTackTextures.MEDIUM));
        event.registerEntityRenderer(ModEntities.SMALL_ZOMBIE_HORSE.get(), context -> new UndeadHorseRenderer(context,
                BhModelLayers.SMALL_UNDEAD_HORSE, BhModelLayers.SMALL_SADDLE, BhModelLayers.SMALL_CARPET,
                BhModelLayers.SMALL_ARMOR, BhModelLayers.SMALL_CHEST, BhTackTextures.SMALL));
        for (var kind : List.of(ModEntities.STRAYS, ModEntities.SCULKS,
                ModEntities.HUSKS, ModEntities.DROWNEDS)) {
            event.registerEntityRenderer(kind.get(0).get(), context -> new UndeadHorseRenderer(context,
                    BhModelLayers.LARGE_UNDEAD_HORSE, BhModelLayers.PERCHERON_SADDLE, BhModelLayers.PERCHERON_CARPET,
                    BhModelLayers.PERCHERON_ARMOR, BhModelLayers.PERCHERON_CHEST, BhTackTextures.PERCHERON));
            event.registerEntityRenderer(kind.get(1).get(), context -> new UndeadHorseRenderer(context,
                    BhModelLayers.MEDIUM_UNDEAD_HORSE, BhModelLayers.MEDIUM_SADDLE, BhModelLayers.MEDIUM_CARPET,
                    BhModelLayers.MEDIUM_ARMOR, BhModelLayers.MEDIUM_CHEST, BhTackTextures.MEDIUM));
            event.registerEntityRenderer(kind.get(2).get(), context -> new UndeadHorseRenderer(context,
                    BhModelLayers.SMALL_UNDEAD_HORSE, BhModelLayers.SMALL_SADDLE, BhModelLayers.SMALL_CARPET,
                    BhModelLayers.SMALL_ARMOR, BhModelLayers.SMALL_CHEST, BhTackTextures.SMALL));
        }
        event.registerEntityRenderer(ModEntities.ANDALUSIAN_DONKEY.get(), context -> new DonkeyRenderer(context,
                BhModelLayers.ANDALUSIAN_DONKEY, BhModelLayers.ANDALUSIAN_DONKEY_BABY,
                BhModelLayers.ANDALUSIAN_DONKEY_SADDLE, BhModelLayers.ANDALUSIAN_DONKEY_SADDLE_BABY,
                BhModelLayers.ANDALUSIAN_DONKEY_CARPET, BhModelLayers.ANDALUSIAN_DONKEY_CARPET_BABY,
                BhModelLayers.ANDALUSIAN_DONKEY_ARMOR, BhModelLayers.ANDALUSIAN_DONKEY_ARMOR_BABY,
                BhModelLayers.ANDALUSIAN_DONKEY_CHEST, BhModelLayers.ANDALUSIAN_DONKEY_CHEST_BABY,
                BhTackTextures.ANDALUSIAN_DONKEY));
        event.registerEntityRenderer(ModEntities.ASINARA_DONKEY.get(), context -> new DonkeyRenderer(context,
                BhModelLayers.ASINARA_DONKEY, BhModelLayers.ASINARA_DONKEY_BABY,
                BhModelLayers.ASINARA_DONKEY_SADDLE, BhModelLayers.ASINARA_DONKEY_SADDLE_BABY,
                BhModelLayers.ASINARA_DONKEY_CARPET, BhModelLayers.ASINARA_DONKEY_CARPET_BABY,
                BhModelLayers.ASINARA_DONKEY_ARMOR, BhModelLayers.ASINARA_DONKEY_ARMOR_BABY,
                BhModelLayers.ASINARA_DONKEY_CHEST, BhModelLayers.ASINARA_DONKEY_CHEST_BABY,
                BhTackTextures.ASINARA_DONKEY));
        event.registerEntityRenderer(ModEntities.MAMMOTH_DONKEY.get(), context -> new DonkeyRenderer(context,
                LargeDonkeyModel::new, BhModelLayers.MAMMOTH_DONKEY, BhModelLayers.MAMMOTH_DONKEY_BABY,
                BhModelLayers.MAMMOTH_DONKEY_SADDLE, BhModelLayers.MAMMOTH_DONKEY_SADDLE_BABY,
                BhModelLayers.MAMMOTH_DONKEY_CARPET, BhModelLayers.MAMMOTH_DONKEY_CARPET_BABY,
                BhModelLayers.MAMMOTH_DONKEY_ARMOR, BhModelLayers.MAMMOTH_DONKEY_ARMOR_BABY,
                BhModelLayers.MAMMOTH_DONKEY_CHEST, BhModelLayers.MAMMOTH_DONKEY_CHEST_BABY,
                BhTackTextures.MAMMOTH_DONKEY));
        event.registerEntityRenderer(ModEntities.MINIATURE_DONKEY.get(), context -> new DonkeyRenderer(context,
                MiniatureDonkeyModel::new, BhModelLayers.MINIATURE_DONKEY, BhModelLayers.MINIATURE_DONKEY_BABY,
                BhModelLayers.MINIATURE_DONKEY_SADDLE, BhModelLayers.MINIATURE_DONKEY_SADDLE_BABY,
                BhModelLayers.MINIATURE_DONKEY_CARPET, BhModelLayers.MINIATURE_DONKEY_CARPET_BABY,
                BhModelLayers.MINIATURE_DONKEY_ARMOR, BhModelLayers.MINIATURE_DONKEY_ARMOR_BABY,
                BhModelLayers.MINIATURE_DONKEY_CHEST, BhModelLayers.MINIATURE_DONKEY_CHEST_BABY,
                BhTackTextures.MINIATURE_DONKEY));
        event.registerEntityRenderer(ModEntities.PEGA_DONKEY.get(), context -> new DonkeyRenderer(context,
                LargeDonkeyModel::new, BhModelLayers.PEGA_DONKEY, BhModelLayers.PEGA_DONKEY_BABY,
                BhModelLayers.PEGA_DONKEY_SADDLE, BhModelLayers.PEGA_DONKEY_SADDLE_BABY,
                BhModelLayers.PEGA_DONKEY_CARPET, BhModelLayers.PEGA_DONKEY_CARPET_BABY,
                BhModelLayers.PEGA_DONKEY_ARMOR, BhModelLayers.PEGA_DONKEY_ARMOR_BABY,
                BhModelLayers.PEGA_DONKEY_CHEST, BhModelLayers.PEGA_DONKEY_CHEST_BABY,
                BhTackTextures.PEGA_DONKEY));
        event.registerEntityRenderer(ModEntities.POITOU_DONKEY.get(), context -> new DonkeyRenderer(context,
                LargeDonkeyModel::new, BhModelLayers.POITOU_DONKEY, BhModelLayers.POITOU_DONKEY_BABY,
                BhModelLayers.POITOU_DONKEY_SADDLE, BhModelLayers.POITOU_DONKEY_SADDLE_BABY,
                BhModelLayers.POITOU_DONKEY_CARPET, BhModelLayers.POITOU_DONKEY_CARPET_BABY,
                BhModelLayers.POITOU_DONKEY_ARMOR, BhModelLayers.POITOU_DONKEY_ARMOR_BABY,
                BhModelLayers.POITOU_DONKEY_CHEST, BhModelLayers.POITOU_DONKEY_CHEST_BABY,
                BhTackTextures.POITOU_DONKEY));
        event.registerEntityRenderer(ModEntities.ZAMORANO_DONKEY.get(), context -> new DonkeyRenderer(context,
                BhModelLayers.ZAMORANO_DONKEY, BhModelLayers.ZAMORANO_DONKEY_BABY,
                BhModelLayers.ZAMORANO_DONKEY_SADDLE, BhModelLayers.ZAMORANO_DONKEY_SADDLE_BABY,
                BhModelLayers.ZAMORANO_DONKEY_CARPET, BhModelLayers.ZAMORANO_DONKEY_CARPET_BABY,
                BhModelLayers.ZAMORANO_DONKEY_ARMOR, BhModelLayers.ZAMORANO_DONKEY_ARMOR_BABY,
                BhModelLayers.ZAMORANO_DONKEY_CHEST, BhModelLayers.ZAMORANO_DONKEY_CHEST_BABY,
                BhTackTextures.ZAMORANO_DONKEY));

        for (int i = 0; i < ModEntities.MULES.size(); i++) {
            int size = i;
            event.registerEntityRenderer(ModEntities.MULES.get(i).get(), ctx -> new MuleRenderer(ctx, size));
        }
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
    public static void registerItemColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> -1, Items.SKELETON_HORSE_SPAWN_EGG,
                Items.ZOMBIE_HORSE_SPAWN_EGG, Items.MULE_SPAWN_EGG);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        BhModelLayers.register(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.CART.get(), CartScreen::new);
            if (ModList.get().isLoaded("cloth_config")) {
                ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, parent) -> BhConfigScreen.create(parent)));
            }
        });
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            BhTackTextures.clearCache();
            BhNamedCoats.clearCache();
            BhBreedCarpets.clearCache();
            BhJumpClips.load(manager);
        });
    }

    public static void sendChargeChoice() {
        if (Minecraft.getInstance().getConnection() != null) {
            BhNetworking.sendToServer(new BhChargePayload(BhConfig.ownFeature(BhFeature.HORSE_CHARGE)));
        }
    }

    public static void onDisconnect() {
        BhConfig.dropServer();
        if (tookServerBreeds) {
            BhBreedData.resetToBuiltIn();
            ArchetypeType.resetSpeeds();
            tookServerBreeds = false;
        }
        BhClientCaches.resetAll();
    }
}
