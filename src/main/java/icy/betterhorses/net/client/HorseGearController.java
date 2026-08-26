package icy.betterhorses.net.client;

import icy.betterhorses.net.BhGears;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.network.HorseGearPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;

public final class HorseGearController {

    public static final HorseGearController INSTANCE = new HorseGearController();

    private static final int NO_HORSE = Integer.MIN_VALUE;

    private int selectedGear;
    private int appliedGear;
    private int gearedHorseId = NO_HORSE;

    private HorseGearController() {}

    public int shiftUp(AbstractHorse horse) {
        if (horse.getId() != gearedHorseId) {
            gearedHorseId = horse.getId();
            selectedGear = 0;
            appliedGear = 0;
        }
        selectedGear = BhGears.next(selectedGear);
        apply(horse, selectedGear);
        return selectedGear;
    }

    public boolean isEngaged() {
        return selectedGear > 0;
    }

    public void reset() {
        selectedGear = 0;
        appliedGear = 0;
        gearedHorseId = NO_HORSE;
    }

    public Output tick(boolean eligible, @Nullable AbstractHorse horse, boolean forwardDown, boolean backDown) {
        if (!eligible || horse == null) {
            reset();
            return Output.PASS_THROUGH;
        }

        if (horse.getId() != gearedHorseId) {
            gearedHorseId = horse.getId();
            selectedGear = 0;
            appliedGear = 0;
        }

        int effectiveGear = forwardDown || backDown ? 0 : selectedGear;
        IHorseData data = IHorseData.of(horse);
        if (effectiveGear != appliedGear
                || data.bh_getGear() != effectiveGear
                || data.bh_getGaitGear() != selectedGear) {
            apply(horse, effectiveGear);
        }

        return effectiveGear > 0 ? Output.GEARED : Output.PASS_THROUGH;
    }

    private void apply(AbstractHorse horse, int gear) {
        appliedGear = gear;
        IHorseData data = IHorseData.of(horse);
        data.bh_setGear(gear);
        data.bh_setGaitGear(selectedGear);
        ClientPlayNetworking.send(new HorseGearPayload(horse.getId(), gear, selectedGear));
    }

    public enum Output {
        GEARED,
        PASS_THROUGH;

        public boolean geared() {
            return this == GEARED;
        }
    }

    static {
        BhClientCaches.register(INSTANCE::reset);
    }
}
