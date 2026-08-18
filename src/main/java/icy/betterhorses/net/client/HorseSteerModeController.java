package icy.betterhorses.net.client;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.network.BhSteerModePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;

/**
 * Keeps the horse's steer mode in step with which camera the rider is using.
 *
 * <p>In third person the camera is free - it never turns the horse - and A/D steer instead. In
 * first person the horse is pointed with the view, as vanilla does it. That split is the whole
 * feature; this class exists only to get the fact across to the server, because
 * {@code getRiddenRotation} runs on both sides and only this client knows which camera is active.
 *
 * <p>Edge-triggered. The flag changes when you press F5, which is rare, so sending it per tick
 * would be one wasted packet every tick of every ride for the lifetime of the world.
 */
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

    /**
     * @param horse the horse this client is controlling, or null if not riding one
     */
    public void tick(@Nullable AbstractHorse horse) {
        if (horse == null) {
            reset();
            return;
        }

        boolean freeSteer = !Minecraft.getInstance().options.getCameraType().isFirstPerson();
        IHorseData data = (IHorseData) horse;

        // Resend on a horse change too: mounting a different animal starts from its own synced
        // default, which will not match what this rider's camera is doing.
        boolean horseChanged = horse.getId() != trackedHorseId;
        if (!horseChanged && freeSteer == sentFreeSteer && data.bh_isFreeSteer() == freeSteer) {
            return;
        }

        trackedHorseId = horse.getId();
        sentFreeSteer = freeSteer;
        // Set locally as well as sending: the riding client drives its own horse's rotation and
        // must not spend a round trip in the wrong steering mode every time F5 is pressed.
        data.bh_setFreeSteer(freeSteer);
        ClientPlayNetworking.send(new BhSteerModePayload(horse.getId(), freeSteer));
    }
}
