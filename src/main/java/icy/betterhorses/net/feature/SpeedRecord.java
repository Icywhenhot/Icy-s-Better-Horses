package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhCriteria;
import icy.betterhorses.net.IHorseData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class SpeedRecord implements HorseFeature {

    private static final double SPEED_DISPLAY_FACTOR = 43.2D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        if (horse.level().isClientSide() || horse.tickCount % 20 != 0) {
            return;
        }
        if (!(horse.getControllingPassenger() instanceof ServerPlayer rider)) {
            return;
        }
        long blocksPerSecond = Math.round(
                horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * SPEED_DISPLAY_FACTOR);
        BhCriteria.fire(rider, BhCriteria.TOP_SPEED, (int) blocksPerSecond);
    }
}
