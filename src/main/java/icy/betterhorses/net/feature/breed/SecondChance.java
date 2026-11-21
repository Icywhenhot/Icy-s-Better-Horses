package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class SecondChance implements BreedAbility {

    private static final Tune DURATION = Tune.of("second_chance", "resistance_ticks", 100.0D).within(41.0D, 72000.0D);
    private static final Tune LEVEL = Tune.of("second_chance", "resistance_level", 1.0D, 2.0D, 2.0D).within(1.0D, 5.0D);
    private static final int REFRESH = 40;

    private boolean hadRider;
    private boolean wasHurt;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player up = BhBreedAbilities.rider(horse);
        if (up != null && !hadRider) {
            BhSurge.pulse(data, 0, 1);
        }
        hadRider = up != null;

        boolean hurt = horse.hurtTime > 0 || (up != null && up.hurtTime > 0);
        if (hurt && !wasHurt) {
            BhSurge.pulse(data, 0, 1);
        }
        wasHurt = hurt;

        if (horse.tickCount % REFRESH != 0) {
            return;
        }
        Player rider = up;
        if (rider == null) {
            return;
        }
        int guard = BhAbility.ANDALUSIAN_GUARD.tier(data);
        if (guard < 0) {
            return;
        }
        BhBreedAbilities.applyQuietEffect(rider, MobEffects.DAMAGE_RESISTANCE, DURATION.ticks(), LEVEL.ticks(guard) - 1);
    }

}
