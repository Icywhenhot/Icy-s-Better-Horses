package icy.betterhorses.net;

import icy.betterhorses.net.client.ChargeShakeController;
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
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import icy.betterhorses.net.network.HorseChargeShakePayload;
import icy.betterhorses.net.network.HorseManageResultPayload;
import icy.betterhorses.net.network.HorseRosterSyncPayload;
import icy.betterhorses.net.network.BreedDataPayload;
import icy.betterhorses.net.network.ConfigSyncPayload;
import icy.betterhorses.net.network.TrustSyncPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import icy.betterhorses.net.entity.HorseCartEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;
import com.klikli_dev.modonomicon.client.render.page.PageRendererRegistry;
import icy.betterhorses.net.book.BhBreedCoatsPage;
import icy.betterhorses.net.book.BhCartModelsPage;
import icy.betterhorses.net.book.BhChargeMeterPage;
import icy.betterhorses.net.client.BhClientCaches;
import icy.betterhorses.net.client.book.BhBreedCoatsPageRenderer;
import icy.betterhorses.net.client.book.BhCartModelsPageRenderer;
import icy.betterhorses.net.client.book.BhChargeMeterPageRenderer;

@Mod(value = IcysBetterHorses.MOD_ID, dist = Dist.CLIENT)
public class IcysBetterHorsesClient {

    private static final String CATEGORY = "key.categories.icys-better-horses";
    private static final double RADIAL_REACH = 12.0D;
    private static final ResourceLocation BOOK_MODEL = ResourceLocation.fromNamespaceAndPath(
            IcysBetterHorses.RESOURCE_NAMESPACE, "item/stable_handbook_book");

    public static KeyMapping CALL_KEY;
    public static KeyMapping RADIAL_KEY;
    public static KeyMapping MANAGE_KEY;
    public static KeyMapping GEAR_KEY;
    public static KeyMapping REAR_KEY;
    public static KeyMapping FREE_LOOK_KEY;
    public static KeyMapping CART_SIZE_KEY;

    private static final double BH_ROUSE_SCAN = 32.0D;

    private boolean callKeyWasDown = false;
    private static boolean tookServerBreeds = false;

    public IcysBetterHorsesClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerKeyMappings);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(BhModelLayers::register);
        modEventBus.addListener(this::registerMenuScreens);
        modEventBus.addListener(this::registerBookModel);
        modEventBus.addListener(this::aliasBookModel);
        modEventBus.addListener(this::registerItemColors);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onDisconnect);
        NeoForge.EVENT_BUS.addListener(this::onEntityLeave);
        if (ModList.get().isLoaded("cloth_config")) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                    (container, parent) -> icy.betterhorses.net.client.BhConfigScreen.create(parent));
        }

        PageRendererRegistry.registerPageRenderer(
                BhBreedCoatsPage.ID,
                page -> new BhBreedCoatsPageRenderer((BhBreedCoatsPage) page));
        PageRendererRegistry.registerPageRenderer(
                BhCartModelsPage.ID,
                page -> new BhCartModelsPageRenderer((BhCartModelsPage) page));
        PageRendererRegistry.registerPageRenderer(
                BhChargeMeterPage.ID,
                page -> new BhChargeMeterPageRenderer((BhChargeMeterPage) page));
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        CALL_KEY = new KeyMapping(
                "key.icys-better-horses.call",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                CATEGORY);
        RADIAL_KEY = new KeyMapping(
                "key.icys-better-horses.radial",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY);
        MANAGE_KEY = new KeyMapping(
                "key.icys-better-horses.manage",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                CATEGORY);

        GEAR_KEY = new KeyMapping(
                "key.icys-better-horses.gear",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                CATEGORY);

        REAR_KEY = new KeyMapping(
                "key.icys-better-horses.rear",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                CATEGORY);

        FREE_LOOK_KEY = new KeyMapping(
                "key.icys-better-horses.free_look",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                CATEGORY);

        CART_SIZE_KEY = new KeyMapping(
                "key.icys-better-horses.cart_size",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                CATEGORY);

        event.register(CALL_KEY);
        event.register(RADIAL_KEY);
        event.register(MANAGE_KEY);
        event.register(GEAR_KEY);
        event.register(REAR_KEY);
        event.register(FREE_LOOK_KEY);
        event.register(CART_SIZE_KEY);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HORSE_CART, HorseCartRenderer::new);
        event.registerEntityRenderer(ModEntities.ICELANDIC_HORSE, context ->
                new IcelandicHorseRenderer(context,
                        BhModelLayers.ICELANDIC_HORSE,
                        BhModelLayers.ICELANDIC_HORSE_BABY));
        event.registerEntityRenderer(ModEntities.FRIESIAN_HORSE, context ->
                new FriesianHorseRenderer(context,
                        BhModelLayers.FRIESIAN_HORSE,
                        BhModelLayers.FRIESIAN_HORSE_BABY));

        event.registerEntityRenderer(ModEntities.HAFLINGER_HORSE, HaflingerHorseRenderer::new);

        event.registerEntityRenderer(ModEntities.APPALOOSA_HORSE, MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.THOROUGHBRED_HORSE, MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.AMERICAN_PAINT_HORSE, MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.ANDALUSIAN_HORSE, MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSTANG_HORSE, MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.QUARTER_HORSE, MediumHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.ARABIAN_HORSE, SmallHorseRenderer::new);
        event.registerEntityRenderer(ModEntities.MORGAN_HORSE, SmallHorseRenderer::new);

        event.registerEntityRenderer(ModEntities.PERCHERON_HORSE, context ->
                new PercheronHorseRenderer(context,
                        BhModelLayers.PERCHERON_HORSE,
                        BhModelLayers.PERCHERON_HORSE_BABY));

        event.registerEntityRenderer(ModEntities.SHIRE_HORSE, context ->
                new ShireHorseRenderer(context,
                        BhModelLayers.SHIRE_HORSE,
                        BhModelLayers.SHIRE_HORSE_BABY));

        event.registerEntityRenderer(ModEntities.BELGIAN_HORSE, context ->
                new BelgianHorseRenderer(context,
                        BhModelLayers.BELGIAN_HORSE,
                        BhModelLayers.BELGIAN_HORSE_BABY));

        event.registerEntityRenderer(ModEntities.CLYDESDALE_HORSE, context ->
                new ClydesdaleHorseRenderer(context,
                        BhModelLayers.CLYDESDALE_HORSE,
                        BhModelLayers.CLYDESDALE_HORSE_BABY));
    }

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.CART_CHEST, CartChestScreen::new);
    }

    private void registerBookModel(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(BOOK_MODEL));
    }

    private void aliasBookModel(ModelEvent.ModifyBakingResult event) {
        BakedModel baked = event.getModels().get(ModelResourceLocation.standalone(BOOK_MODEL));
        if (baked == null) {
            return;
        }
        event.getModels().put(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(
                IcysBetterHorses.RESOURCE_NAMESPACE, "stable_handbook_book")), baked);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> -1,
                ModItems.BREED_SPAWN_EGGS.toArray(new Item[0]));
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

    private void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        BhConfig.dropServer();
        if (tookServerBreeds) {
            BhBreedData.resetToBuiltIn();
            tookServerBreeds = false;
        }
        BhClientCaches.resetAll();
    }

    private void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();
        int previewId = icy.betterhorses.net.client.render.BhHorseRenderState.previewId(entity.getId());
        icy.betterhorses.net.client.render.BhEquineGait.remove(entity.getId());
        icy.betterhorses.net.client.render.BhEquineGait.remove(previewId);
        icy.betterhorses.net.client.render.BhRiderMotion.remove(entity.getId());
        icy.betterhorses.net.client.render.BhRiderMotion.remove(previewId);
        icy.betterhorses.net.client.render.BhHorseRenderState.remove(entity.getId());
        if (entity instanceof AbstractHorse horse) {
            icy.betterhorses.net.client.render.HorseStabilizerAnimatable.remove(horse);
        }
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        HorseStabilizerSoundController.tick(client);
        if (client.player == null || client.level == null) return;

        boolean callKeyDown = CALL_KEY.isDown();
        if (callKeyDown && !callKeyWasDown) {
            if (bh_anyHorseRoused(client)) {
                PacketDistributor.sendToServer(new HorseRecallPayload());
            } else if (BhHorseKind.managed(client.player.getVehicle())
                    && client.player.getVehicle() instanceof AbstractHorse mount) {
                client.setScreen(new HorseInfoScreen(mount));
            } else {
                PacketDistributor.sendToServer(new CallHorsePayload());
            }
        }
        callKeyWasDown = callKeyDown;
        while (CALL_KEY.consumeClick()) {}

        while (RADIAL_KEY.consumeClick()) {
            bh_tryOpenRadial(client);
        }

        while (MANAGE_KEY.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new HorseRosterScreen());
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
        if (player == null || client.screen != null) {
            return;
        }
        if (!BhHorseKind.managed(player.getControlledVehicle())
                || !(player.getControlledVehicle() instanceof AbstractHorse horse)
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

    private static void bh_tryRear(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null) {
            return;
        }

        AbstractHorse horse = BhHorseKind.managed(player.getControlledVehicle())
                && player.getControlledVehicle() instanceof AbstractHorse mount
                ? mount
                : bh_lookedAtHorse(player);
        if (horse == null) {
            return;
        }

        PacketDistributor.sendToServer(new BhRearPayload(horse.getId()));
    }

    private static void bh_trySwapCartSize(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.screen != null) {
            return;
        }

        Entity target = bh_lookedAtCartTarget(player);
        if (target == null) {
            return;
        }

        PacketDistributor.sendToServer(new CartSizePayload(target.getId()));
    }

    private static @Nullable Entity bh_lookedAtCartTarget(LocalPlayer player) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(RADIAL_REACH));
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(RADIAL_REACH)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, eye, end, searchBox,
                entity -> (entity instanceof HorseCartEntity || BhHorseKind.managed(entity))
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
        if (player == null || client.screen != null) {
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
}
