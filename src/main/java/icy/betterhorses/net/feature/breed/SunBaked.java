package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class SunBaked implements BreedAbility {

    private static final int REFRESH = 20;
    private static final Tune LINGER = Tune.of("sun_baked", "fire_resistance_ticks", 60.0D).within(21.0D, 72000.0D);

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide() || horse.tickCount % REFRESH != 0 || !data.bh_getBreed().isHusk()) {
            return;
        }
        boolean self = BhAbility.HUSK_FIREPROOF.on();
        Player rider = BhBreedAbilities.rider(horse);
        boolean shared = rider != null && BhAbility.HUSK_SHARE.on()
                && BhHorseTraits.bondTier(data.bh_getBond()) >= 2;
        if (self) {
            BhBreedAbilities.applyQuietEffect(horse, MobEffects.FIRE_RESISTANCE, LINGER.ticks(), 0);
        }
        if (shared) {
            BhBreedAbilities.applyQuietEffect(rider, MobEffects.FIRE_RESISTANCE, LINGER.ticks(), 0);
        }
        int packed = 0;
        if (shared) {
            packed = BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 1);
        } else if (self && horse.isOnFire()) {
            packed = BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 0);
        }
        BhSurge.set(data, BhContent.SUN_BAKED.getKey(), packed);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        BhSurge.set(data, BhContent.SUN_BAKED.getKey(), 0);
    }
}
