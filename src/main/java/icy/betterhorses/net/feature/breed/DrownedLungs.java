package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

public final class DrownedLungs implements BreedAbility {

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide()) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        boolean gills = BhHorseTraits.bondTier(data.bh_getBond()) >= 2 && BhAbility.DROWNED_GILLS.on();
        int packed = 0;
        if (rider != null && rider.isEyeInFluid(FluidTags.WATER) && (gills || BhAbility.DROWNED_BREATH.on())) {
            packed = BhSurge.pack(BhSurge.ACTIVE, 0, 0, gills ? 0 : 50, gills ? 1 : 0);
        }
        BhSurge.set(data, BhContent.DROWNED_LUNGS.getKey(), packed);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        BhSurge.set(data, BhContent.DROWNED_LUNGS.getKey(), 0);
    }

    public static void breathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !(player.getVehicle() instanceof AbstractHorse horse)
                || !player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getBreed().isDrowned()) {
            return;
        }
        if (BhHorseTraits.bondTier(data.bh_getBond()) >= 2 && BhAbility.DROWNED_GILLS.on()) {
            event.setCanBreathe(true);
            event.setRefillAirAmount(Math.max(event.getRefillAirAmount(), 4));
        } else if (!event.canBreathe() && BhAbility.DROWNED_BREATH.on()) {
            int drain = event.getConsumeAirAmount();
            event.setConsumeAirAmount(drain / 2 + ((player.tickCount & 1) == 0 ? drain % 2 : 0));
        }
    }
}
