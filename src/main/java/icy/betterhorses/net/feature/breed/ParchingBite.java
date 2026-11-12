package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhDamageTypes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class ParchingBite implements BreedAbility {

    private static final int HUNGER_TICKS = 140;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
    }

    public static void hit(LivingEntity target, DamageSource src, float amount) {
        if (!(src.getDirectEntity() instanceof AbstractHorse horse) || amount <= 0.0F
                || !target.isAlive() || !BhAbility.HUSK_THIRST.on()
                || src.is(DamageTypeTags.IS_EXPLOSION) || src.is(DamageTypeTags.IS_PROJECTILE)
                || !(src.is(BhDamageTypes.HORSE_BASH) || src.is(BhDamageTypes.HORSE_KICK) || src.getEntity() == horse)
                || target == horse || horse.hasIndirectPassenger(target) || horse.isAlliedTo(target)) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getBreed().isHusk()) {
            return;
        }
        BhBreedAbilities.applyQuietEffect(target, MobEffects.HUNGER, HUNGER_TICKS, 0);
        BhSurge.pulse(data, BhContent.PARCHING_BITE.key(), 0);
    }
}
