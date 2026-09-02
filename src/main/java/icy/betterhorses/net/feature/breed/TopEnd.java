package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class TopEnd implements BreedAbility {

    private static final String KEY = "top_end";
    private static final int COOLDOWN = 100;
    private static final int BURST = 60;
    private static final int LONG_BURST = 100;

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
                cooldown = COOLDOWN;
            }
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, burst, span, bonus));
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            data.bh_setSurge(BhSurge.pack(BhSurge.COOLING, cooldown, COOLDOWN, bonus));
            return;
        }
        data.bh_setSurge(0);
        if (BhBreedAbilities.rider(horse) == null || !state.gallopingFlat(horse, data)) {
            return;
        }
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        burst = tier >= 2 ? LONG_BURST : BURST;
        span = burst;
        bonus = tier >= 1 ? 50 : 35;
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
