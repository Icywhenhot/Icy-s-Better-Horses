package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class PegaCompanion implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        if (rider == null || horse.tickCount % 200 != 0) {
            return;
        }
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (tier >= 1 && BhAbility.PEGA_SELF_HEAL.on() && horse.getHealth() < horse.getMaxHealth()) {
            horse.heal(1.0F);
        }
        if (tier >= 2 && BhAbility.PEGA_RIDER_HEAL.on()
                && rider.getHealth() < rider.getMaxHealth()) {
            rider.heal(1.0F);
            BhSurge.pulse(data, 0, 2);
        }
    }

    public static float exhaustion(Player player, float amount) {
        return player.getVehicle() instanceof AbstractHorse horse
                && BhContent.PEGA_DONKEY.key().equals(IHorseData.of(horse).bh_getBreedKey())
                && BhAbility.PEGA_SEAT.on() ? amount * 0.75F : amount;
    }
}
