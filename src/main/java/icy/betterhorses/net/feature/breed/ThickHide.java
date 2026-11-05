package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class ThickHide implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.tickCount % 20 != 0 || !immune(horse)) {
            return;
        }
        for (var effect : horse.getActiveEffects().stream()
                .filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL).toList()) {
            horse.removeEffect(effect.getEffect());
        }
    }

    public static float damageScale(LivingEntity target, DamageSource source) {
        if (!(target instanceof AbstractHorse horse) || !BhAbility.POITOU_HIDE.on()
                || !BhContent.POITOU_DONKEY.getKey().equals(IHorseData.of(horse).bh_getBreedKey())
                || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || !(source.getDirectEntity() instanceof LivingEntity)
                || source.getDirectEntity() != source.getEntity()) {
            return 1.0F;
        }
        return BhHorseTraits.bondTier(IHorseData.of(horse).bh_getBond()) >= 1 ? 0.8F : 0.9F;
    }

    public static boolean immune(LivingEntity target) {
        return target instanceof AbstractHorse horse && BhAbility.POITOU_CLEAR.on()
                && BhContent.POITOU_DONKEY.getKey().equals(IHorseData.of(horse).bh_getBreedKey())
                && BhHorseTraits.bondTier(IHorseData.of(horse).bh_getBond()) >= 2;
    }
}
