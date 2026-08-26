package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.network.BhSteerModePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;

public final class HorseSteerModeController {

    public static final HorseSteerModeController INSTANCE = new HorseSteerModeController();

    private static final int NO_HORSE = Integer.MIN_VALUE;

    private int trackedHorseId = NO_HORSE;
    private boolean sentFreeSteer;

    private HorseSteerModeController() {}

    public void reset() {
        trackedHorseId = NO_HORSE;
        sentFreeSteer = false;
    }

    public void tick(@Nullable AbstractHorse horse) {
        if (horse == null) {
            reset();
            return;
        }

        boolean freeSteer = !Minecraft.getInstance().options.getCameraType().isFirstPerson();
        IHorseData data = IHorseData.of(horse);

        boolean horseChanged = horse.getId() != trackedHorseId;
        if (!horseChanged && freeSteer == sentFreeSteer && data.bh_isFreeSteer() == freeSteer) {
            return;
        }

        trackedHorseId = horse.getId();
        sentFreeSteer = freeSteer;
        data.bh_setFreeSteer(freeSteer);
        ClientPlayNetworking.send(new BhSteerModePayload(horse.getId(), freeSteer));
    }

    static {
        BhClientCaches.register(INSTANCE::reset);
    }
}
