package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class StandstillBurst implements BreedAbility {

    private static final String KEY = "standstill_burst";
    private static final int SETTLE = 20;
    private static final int BURST = 100;
    private static final int COOLDOWN = 200;
    private static final int SHORT_COOLDOWN = 160;

    private int burst;
    private int cooldown;
    private boolean armed;
    private int span;
    private int bonus;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        int tier = BhHorseTraits.bondTier(data.bh_getBond());

        if (burst > 0) {
            burst--;
            if (burst == 0) {
                clear(horse);
                cooldown = tier >= 2 ? SHORT_COOLDOWN : COOLDOWN;
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
        if (state.standstillTicks() >= SETTLE) {
            armed = true;
            data.bh_setSurge(BhSurge.pack(BhSurge.ARMED, 0, 0, tier >= 1 ? 45 : 30));
            return;
        }
        if (!armed || BhBreedAbilities.rider(horse) == null || state.movingTicks() == 0) {
            data.bh_setSurge(0);
            return;
        }
        armed = false;
        burst = BURST;
        span = BURST;
        bonus = tier >= 1 ? 45 : 30;
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.ABILITY, KEY,
                bonus / 100.0D,
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
