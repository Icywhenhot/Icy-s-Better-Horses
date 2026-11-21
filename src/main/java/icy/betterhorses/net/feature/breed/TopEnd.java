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

public final class TopEnd implements BreedAbility {

    private static final String KEY = "top_end";
    private static final Tune BONUS = Tune.of("top_end", "speed_bonus", 0.35D, 0.50D, 0.50D).within(0.0D, 10.0D);
    private static final Tune BURST = Tune.of("top_end", "duration", 60.0D, 60.0D, 100.0D).within(1.0D, 72000.0D);
    private static final Tune COOLDOWN = Tune.of("top_end", "cooldown", 100.0D).within(0.0D, 72000.0D);

    private int burst;
    private int cooldown;
    private int span;
    private int bonus;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (burst > 0) {
            burst--;
            if (burst == 0) {
                clear(horse);
                cooldown = COOLDOWN.ticks();
            }
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, burst, span, bonus));
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            data.bh_setSurge(BhSurge.pack(BhSurge.COOLING, cooldown, COOLDOWN.ticks(), bonus));
            return;
        }
        data.bh_setSurge(0);
        if (!BhAbility.THOROUGHBRED_TOP_END.on(data)
                || BhBreedAbilities.rider(horse) == null || !state.gallopingFlat(horse, data)) {
            return;
        }
        int tier = BhAbility.THOROUGHBRED_TOP_END.tier(data);
        burst = BURST.ticks(tier);
        span = burst;
        bonus = (int) Math.round(BONUS.at(tier) * 100.0D);
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, KEY,
                BONUS.at(tier),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, burst, span, bonus));
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        clear(horse);
        data.bh_setSurge(0);
    }

    private void clear(AbstractHorse horse) {
        BhHorseAttributes.clear(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, KEY);
    }
}
