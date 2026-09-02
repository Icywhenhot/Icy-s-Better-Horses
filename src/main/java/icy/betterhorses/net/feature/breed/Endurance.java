package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class Endurance implements BreedAbility {

    private static final String KEY = "endurance";
    private static final int RAMP = 400;
    private static final int FAST_RAMP = 240;
    private static final int GRACE = 40;
    private static final double DECAY = 0.001D;

    private double bonus;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        double cap = tier >= 1 ? 0.20D : 0.10D;
        int ramp = tier >= 2 ? FAST_RAMP : RAMP;
        int grace = tier >= 2 ? GRACE : 0;

        boolean rolling = BhBreedAbilities.rider(horse) != null
                && (state.movingTicks() > 0 || state.standstillTicks() <= grace);

        double before = bonus;
        if (rolling) {
            bonus = Math.min(cap, bonus + cap / ramp);
        } else {
            bonus = Math.max(0.0D, bonus - DECAY);
        }
        if (bonus != before) {
            BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                    BhHorseAttributes.Source.ABILITY, KEY, bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
        data.bh_setSurge(bonus <= 0.0D ? 0 : BhSurge.pack(BhSurge.ACTIVE,
                (int) Math.round(bonus * 1000.0D),
                (int) Math.round(cap * 1000.0D),
                (int) Math.round(bonus * 100.0D)));
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        bonus = 0.0D;
        data.bh_setSurge(0);
        BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, KEY);
    }
}
