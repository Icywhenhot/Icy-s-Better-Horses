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

public final class StandstillBurst implements BreedAbility {

    private static final String KEY = "standstill_burst";
    private static final Tune SETTLE = Tune.of("standstill_burst", "settle_ticks", 20.0D).within(1.0D, 72000.0D);
    private static final Tune BURST = Tune.of("standstill_burst", "duration", 100.0D).within(1.0D, 72000.0D);
    private static final Tune COOLDOWN = Tune.of("standstill_burst", "cooldown", 200.0D, 200.0D, 160.0D).within(0.0D, 72000.0D);
    private static final Tune BONUS = Tune.of("standstill_burst", "speed_bonus", 0.30D, 0.45D, 0.45D).within(0.0D, 10.0D);

    private int burst;
    private int cooldown;
    private boolean armed;
    private int span;
    private int bonus;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = Math.max(0, BhAbility.QUARTER_BURST.tier(data));

        if (burst > 0) {
            burst--;
            if (burst == 0) {
                clear(horse);
                cooldown = COOLDOWN.ticks(tier);
                span = cooldown;
            }
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, burst, span, bonus));
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            data.bh_setSurge(BhSurge.pack(BhSurge.COOLING, cooldown, span, bonus));
            return;
        }
        if (!BhAbility.QUARTER_BURST.on(data)) {
            data.bh_setSurge(0);
            return;
        }
        if (state.standstillTicks() >= SETTLE.ticks()) {
            armed = true;
            data.bh_setSurge(BhSurge.pack(BhSurge.ARMED, 0, 0, (int) Math.round(BONUS.at(tier) * 100.0D)));
            return;
        }
        if (!armed || BhBreedAbilities.rider(horse) == null || state.movingTicks() == 0) {
            data.bh_setSurge(0);
            return;
        }
        armed = false;
        burst = BURST.ticks(tier);
        span = burst;
        bonus = (int) Math.round(BONUS.at(tier) * 100.0D);
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, KEY,
                BONUS.at(tier),
                AttributeModifier.Operation.MULTIPLY_BASE);
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
