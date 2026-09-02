package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class HardyNorthern implements BreedAbility {

    private static final int SWEEP = 20;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (tier < 1) {
            return;
        }

        horse.setTicksFrozen(0);
        Player rider = BhBreedAbilities.rider(horse);
        if (rider == null) {
            return;
        }
        rider.setTicksFrozen(0);

        if (tier < 2 || horse.tickCount % SWEEP != 0) {
            return;
        }
        List<MobEffectInstance> bad = rider.getActiveEffects().stream()
                .filter(e -> !e.getEffect().value().isBeneficial())
                .toList();
        for (MobEffectInstance effect : bad) {
            rider.removeEffect(effect.getEffect());
        }
        if (!bad.isEmpty()) {
            BhSurge.pulse(data, 0);
        }
    }

    public static boolean blocksFreeze(IHorseData data, int tier) {
        return data.bh_getBreed() == HorseBreed.ICELANDIC && tier >= 1;
    }

    public static boolean blocksBadEffects(LivingEntity rider) {
        return rider.getVehicle() instanceof AbstractHorse horse
                && IHorseData.of(horse).bh_getBreed() == HorseBreed.ICELANDIC
                && BhHorseTraits.bondTier(IHorseData.of(horse).bh_getBond()) >= 2;
    }
}
