package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class Ironclad implements BreedAbility {

    private static final String KEY = "ironclad";
    private static final int REFRESH = 40;
    private static final int SHIELD_DURATION = 100;

    private double applied = -1.0D;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        double armor = horse.getAttributeBaseValue(Attributes.ARMOR);
        if (armor != applied) {
            applied = armor;
            BhHorseAttributes.apply(horse, Attributes.ARMOR,
                    BhHorseAttributes.Source.ABILITY, KEY, armor,
                    AttributeModifier.Operation.ADD_VALUE);
        }

        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (tier < 1 || horse.tickCount % REFRESH != 0) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        if (rider != null && horse.getAttributeValue(Attributes.ARMOR) > 0.0D) {
            BhBreedAbilities.applyQuietEffect(rider, MobEffects.RESISTANCE, SHIELD_DURATION, 0);
        }
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        applied = -1.0D;
        BhHorseAttributes.clear(horse, Attributes.ARMOR, BhHorseAttributes.Source.ABILITY, KEY);
    }

    public static boolean deflectsProjectiles(IHorseData data) {
        return data.bh_getBreed() == HorseBreed.CLYDESDALE
                && BhHorseTraits.bondTier(data.bh_getBond()) >= 2;
    }
}
