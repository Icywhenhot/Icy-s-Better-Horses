package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class EasyKeeper implements BreedAbility {

    private static final String KEY = "adaptable";
    private static final Tune STEP_BONUS = Tune.of("easy_keeper", "step_bonus", 0.5D).within(0.0D, 10.0D);

    private boolean applied;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        boolean want = BhAbility.MORGAN_ADAPT.tier(data) >= 2;
        if (want == applied) {
            return;
        }
        applied = want;
        BhHorseAttributes.apply(horse, Attributes.STEP_HEIGHT,
                BhHorseAttributes.Source.ABILITY, KEY,
                want ? STEP_BONUS.get() : 0.0D, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        applied = false;
        BhHorseAttributes.clear(horse, Attributes.STEP_HEIGHT,
                BhHorseAttributes.Source.ABILITY, KEY);
    }
}
