package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class Skittish implements BreedAbility {

    private static final Tune RIDE_LIMIT = Tune.of("skittish", "ride_ticks", 100.0D).within(1.0D, 72000.0D);

    private int ridden;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (!(horse.getControllingPassenger() instanceof Player rider) || !BhAbility.MINIATURE_SKITTISH.on()) {
            ridden = 0;
            return;
        }
        if (++ridden < RIDE_LIMIT.ticks()) {
            return;
        }
        ridden = 0;
        horse.ejectPassengers();
        horse.makeMad();
        rider.displayClientMessage(Component.translatable("message.icys-better-horses.miniature_buck"), true);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        ridden = 0;
    }
}
