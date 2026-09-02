package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
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

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (horse.tickCount % REFRESH != 0) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        if (rider == null) {
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
