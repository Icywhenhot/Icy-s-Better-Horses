package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
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
                .filter(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL).toList()) {
            horse.removeEffect(effect.getEffect());
        }
    }

    public static float damageScale(LivingEntity target, DamageSource source) {
        int hide = target instanceof AbstractHorse horse
                ? BhAbility.POITOU_HIDE.tier(IHorseData.of(horse), BhContent.POITOU_DONKEY.getKey()) : -1;
        if (hide < 0
                || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_PROJECTILE)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || !(source.getDirectEntity() instanceof LivingEntity)
                || source.getDirectEntity() != source.getEntity()) {
            return 1.0F;
        }
        return hide >= 1 ? 0.8F : 0.9F;
    }

    public static boolean immune(LivingEntity target) {
        return target instanceof AbstractHorse horse
                && BhAbility.POITOU_CLEAR.tier(IHorseData.of(horse), BhContent.POITOU_DONKEY.getKey()) >= 2;
    }
}
