package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhDamageTypes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public final class FrostCharge implements BreedAbility {

    private static final Tune CHILL_TICKS = Tune.of("frost_charge", "slowness_ticks", 100.0D).within(1.0D, 72000.0D);
    private static final Tune CHILL_LEVEL = Tune.of("frost_charge", "slowness_level", 2.0D).within(1.0D, 10.0D);

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
    }

    public static void hit(LivingDamageEvent event) {
        DamageSource src = event.getSource();
        if (!src.is(BhDamageTypes.HORSE_BASH) || !(src.getDirectEntity() instanceof AbstractHorse horse)
                || event.getAmount() <= 0.0F || !event.getEntity().isAlive() || !BhAbility.STRAY_CHILL.on()) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getBreed().isStray()) {
            return;
        }
        BhBreedAbilities.applyQuietEffect(event.getEntity(), MobEffects.MOVEMENT_SLOWDOWN, CHILL_TICKS.ticks(), CHILL_LEVEL.ticks() - 1);
        BhSurge.pulse(data, BhContent.FROST_CHARGE.getKey(), 0);
    }
}
