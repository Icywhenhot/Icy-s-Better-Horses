package icy.betterhorses.net;

import icy.betterhorses.net.client.HorseGearController;
import icy.betterhorses.net.client.HorseInfoScreen;
import icy.betterhorses.net.client.HorseRosterScreen;
import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.entity.HorseCartEntity;
import icy.betterhorses.net.entity.IcelandicHorse;
import icy.betterhorses.net.network.BhRearPayload;
import icy.betterhorses.net.network.CallHorsePayload;
import icy.betterhorses.net.network.CartSizePayload;
import icy.betterhorses.net.network.HorseRecallPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = IcysBetterHorses.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IcysBetterHorsesClientForgeEvents {

    private static final double REACH = 12.0D;
    private static final double ROUSE_SCAN = 48.0D;

    private static boolean callKeyWasDown = false;

    private IcysBetterHorsesClientForgeEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
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

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        IcysBetterHorsesClient.onDisconnect();
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
}
