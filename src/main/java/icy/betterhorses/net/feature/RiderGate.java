package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class RiderGate implements HorseFeature {

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide()) {
            return;
        }
        List<Entity> passengers = horse.getPassengers();
        if (passengers.isEmpty()) {
            return;
        }

        if (!BhConfig.multiRidingEnabled() && passengers.size() > 1) {
            for (int i = 1; i < passengers.size(); i++) {
                passengers.get(i).stopRiding();
            }
            passengers = horse.getPassengers();
            if (passengers.isEmpty()) {
                return;
            }
        }

        if (!BhConfig.horseExclusivityEnabled()) {
            return;
        }

        if (!data.bh_isOwned()) {
            return;
        }
        Entity primary = passengers.get(0);
        if (!(primary instanceof Player)) {
            return;
        }
        if (data.bh_maySaddleUp(primary.getUUID())) {
            return;
        }
        horse.playSound(SoundEvents.HORSE_ANGRY, 1.0F, 1.0F);
        for (Entity passenger : new ArrayList<>(passengers)) {
            passenger.stopRiding();
        }
    }
}
