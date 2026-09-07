package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class SecondChance implements BreedAbility {

    private static final int DURATION = 100;
    private static final int REFRESH = 40;
    private static final int SAVE_COOLDOWN = 2400;

    private int cooldown;
    private boolean hadRider;
    private boolean wasHurt;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (cooldown > 0) {
            cooldown--;
        }
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
        if (!BhAbility.ANDALUSIAN_GUARD.on()) {
            return;
        }
        int amp = BhHorseTraits.bondTier(data.bh_getBond()) >= 1 ? 1 : 0;
        BhBreedAbilities.applyQuietEffect(rider, MobEffects.RESISTANCE, DURATION, amp);
    }

    public boolean ready() {
        return cooldown <= 0;
    }

    public void spend() {
        cooldown = SAVE_COOLDOWN;
    }
}
