package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class Endurance implements BreedAbility {

    private static final String KEY = "endurance";
    private static final Tune CAP = Tune.of("endurance", "max_bonus", 0.10D, 0.20D, 0.20D).within(0.0D, 10.0D);
    private static final Tune RAMP = Tune.of("endurance", "ramp_ticks", 400.0D, 400.0D, 240.0D).within(1.0D, 72000.0D);
    private static final Tune GRACE = Tune.of("endurance", "stop_grace", 0.0D, 0.0D, 40.0D).within(0.0D, 72000.0D);
    private static final Tune DECAY = Tune.of("endurance", "decay_per_tick", 0.001D).within(0.0D, 10.0D);

    private double bonus;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = Math.max(0, BhAbility.ARABIAN_ENDURANCE.tier(data));
        double cap = CAP.at(tier);
        int ramp = RAMP.ticks(tier);
        int grace = GRACE.ticks(tier);

        boolean rolling = BhAbility.ARABIAN_ENDURANCE.on(data)
                && BhBreedAbilities.rider(horse) != null
                && (state.movingTicks() > 0 || state.standstillTicks() <= grace);

        double before = bonus;
        if (rolling) {
            bonus = Math.min(cap, bonus + cap / ramp);
        } else {
            bonus = Math.max(0.0D, bonus - DECAY.get());
        }
        if (bonus != before) {
            BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                    BhHorseAttributes.Source.ABILITY, KEY, bonus,
                    AttributeModifier.Operation.MULTIPLY_BASE);
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
