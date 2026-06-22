package icy.betterhorses.net;

import icy.betterhorses.net.client.HorseInfoScreen;
import icy.betterhorses.net.client.HorseStabilizerSoundController;
import icy.betterhorses.net.network.CallHorsePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = IcysBetterHorses.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IcysBetterHorsesClientForgeEvents {

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

        while (IcysBetterHorsesClient.CALL_KEY.consumeClick()) {
            if (client.player.getVehicle() instanceof AbstractHorse mount) {
                client.setScreen(new HorseInfoScreen(mount));
            } else {
                BhNetworking.sendToServer(new CallHorsePayload());
            }
        }

        while (IcysBetterHorsesClient.RADIAL_KEY.consumeClick()) {
            IcysBetterHorsesClient.bh_tryOpenRadial(client);
        }
    }
}
