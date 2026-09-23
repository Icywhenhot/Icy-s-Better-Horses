package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhHorseAttributes;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class Ironclad implements BreedAbility {

    private static final String KEY = "ironclad";
    private static final int REFRESH = 40;
    private static final int SHIELD_DURATION = 100;

    private double applied = -1.0D;
    private boolean hadRider;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        double armor = BhAbility.CLYDESDALE_ARMOR.on()
                ? armorBonus(horse)
                : 0.0D;
        if (armor != applied) {
            applied = armor;
            BhHorseAttributes.apply(horse, Attributes.ARMOR,
                    BhHorseAttributes.Source.ABILITY, KEY, armor,
                    AttributeModifier.Operation.ADD_VALUE);
        }

        Player up = BhBreedAbilities.rider(horse);
        if (up != null && !hadRider && horse.getAttributeValue(Attributes.ARMOR) > 0.0D) {
            BhSurge.pulse(data, 0, 0);
        }
        hadRider = up != null;

        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (tier < 1 || horse.tickCount % REFRESH != 0) {
            return;
        }
        Player rider = up;
        if (rider != null && BhAbility.CLYDESDALE_RESIST.on()
                && horse.getAttributeValue(Attributes.ARMOR) > 0.0D) {
            BhBreedAbilities.applyQuietEffect(rider, MobEffects.DAMAGE_RESISTANCE, SHIELD_DURATION, 0);
        }
    }

    private static double armorBonus(AbstractHorse horse) {
        double[] armor = {0.0D};
        horse.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.BODY).forEachModifier(
                net.minecraft.world.entity.EquipmentSlot.BODY, (attr, modifier) -> {
                    if (attr.equals(Attributes.ARMOR) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        armor[0] += modifier.amount();
                    }
                });
        return armor[0] * 0.25D;
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        applied = -1.0D;
        BhHorseAttributes.clear(horse, Attributes.ARMOR, BhHorseAttributes.Source.ABILITY, KEY);
    }

    public static boolean deflectsProjectiles(IHorseData data) {
        return Objects.equals(data.bh_getBreedKey(), BhContent.CLYDESDALE.getKey())
                && BhHorseTraits.bondTier(data.bh_getBond()) >= 2
                && BhAbility.CLYDESDALE_DEFLECT.on();
    }
}
