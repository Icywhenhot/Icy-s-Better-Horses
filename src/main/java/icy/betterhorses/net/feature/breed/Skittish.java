package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class Skittish implements BreedAbility {

    private static final int RIDE_LIMIT = 100;

    private int ridden;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.getControllingPassenger() instanceof Player rider) || !BhAbility.MINIATURE_SKITTISH.on()) {
            ridden = 0;
            return;
        }
        if (++ridden < RIDE_LIMIT) {
            return;
        }
        ridden = 0;
        horse.ejectPassengers();
        horse.makeMad();
        rider.sendOverlayMessage(Component.translatable("message.icys-better-horses.miniature_buck"));
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        ridden = 0;
    }
}
