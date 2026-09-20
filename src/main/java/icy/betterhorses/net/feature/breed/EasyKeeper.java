package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAttributes;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class EasyKeeper implements BreedAbility {

    private static final String KEY = "adaptable";
    private static final double STEP_BONUS = 0.5D;

    private boolean applied;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        boolean want = BhHorseTraits.bondTier(data.bh_getBond()) >= 2 && BhAbility.MORGAN_ADAPT.on();
        if (want == applied) {
            return;
        }
        applied = want;
        BhHorseAttributes.apply(horse, BhAttributes.STEP_HEIGHT_ADDITION,
                BhHorseAttributes.Source.ABILITY, KEY,
                want ? STEP_BONUS : 0.0D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        applied = false;
        BhHorseAttributes.clear(horse, BhAttributes.STEP_HEIGHT_ADDITION,
                BhHorseAttributes.Source.ABILITY, KEY);
    }
}
