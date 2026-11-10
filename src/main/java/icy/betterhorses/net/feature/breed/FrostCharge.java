package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhDamageTypes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class FrostCharge implements BreedAbility {

    private static final int CHILL_TICKS = 100;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
    }

    public static void hit(LivingDamageEvent.Post event) {
        DamageSource src = event.getSource();
        if (!src.is(BhDamageTypes.HORSE_BASH) || !(src.getDirectEntity() instanceof AbstractHorse horse)
                || event.getNewDamage() <= 0.0F || !event.getEntity().isAlive() || !BhAbility.STRAY_CHILL.on()) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getBreed().isStray()) {
            return;
        }
        BhBreedAbilities.applyQuietEffect(event.getEntity(), MobEffects.MOVEMENT_SLOWDOWN, CHILL_TICKS, 1);
        BhSurge.pulse(data, BhContent.FROST_CHARGE.getKey(), 0);
    }
}
