package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorsesClient;
import icy.betterhorses.net.network.BhFreeLookPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;

public final class HorseFreeLookController {

    public static final HorseFreeLookController INSTANCE = new HorseFreeLookController();

    private static final int NO_HORSE = Integer.MIN_VALUE;

    private int trackedHorseId = NO_HORSE;
    private boolean sent;

    private HorseFreeLookController() {}

    public void reset() {
        trackedHorseId = NO_HORSE;
        sent = false;
    }

    public void tick(@Nullable AbstractHorse horse) {
        if (horse == null) {
            reset();
            return;
        }

        boolean freeLook = IcysBetterHorsesClient.FREE_LOOK_KEY.isDown();
        IHorseData data = IHorseData.of(horse);

        boolean horseChanged = horse.getId() != trackedHorseId;
        if (!horseChanged && freeLook == sent && data.bh_isFreeLook() == freeLook) {
            return;
        }

        trackedHorseId = horse.getId();
        sent = freeLook;
        data.bh_setFreeLook(freeLook);
        ClientPlayNetworking.send(new BhFreeLookPayload(horse.getId(), freeLook));
    }

    static {
        BhClientCaches.register(INSTANCE::reset);
    }
}
