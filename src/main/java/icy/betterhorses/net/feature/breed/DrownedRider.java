package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

public final class DrownedRider implements BreedAbility {

    private final WaterDash dash = new WaterDash();

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        Player rider = BhBreedAbilities.rider(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (dash.push(horse, rider, eligible(horse, data, rider))) {
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, dash.dash, WaterDash.DASH_TICKS, 0, 2));
        } else if (dash.cooldown > 0 && BhAbility.SKELETON_DASH.on()) {
            data.bh_setSurge(BhSurge.pack(BhSurge.COOLING, dash.cooldown, WaterDash.COOLDOWN, 0, 2));
        } else if (rider != null && rider.isEyeInFluid(FluidTags.WATER)
                && (tier >= 1 ? BhAbility.SKELETON_DIVE.on() : BhAbility.SKELETON_BREATH.on())) {
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, 0, 0, tier >= 1 ? 0 : 50, tier >= 1 ? 1 : 0));
        } else {
            data.bh_setSurge(0);
        }
    }

    @Override
    public boolean hasActiveSkill() {
        return true;
    }

    @Override
    public void onActivate(AbstractHorse horse, IHorseData data) {
        Player rider = BhBreedAbilities.rider(horse);
        if (!horse.level().isClientSide() && dash.cooldown <= 0 && eligible(horse, data, rider)) {
            dash.start();
        }
    }

    private static boolean eligible(AbstractHorse horse, IHorseData data, Player rider) {
        return data.bh_getBreed().isSkeleton() && BhHorseTraits.bondTier(data.bh_getBond()) >= 2
                && BhAbility.SKELETON_DASH.on() && WaterDash.underwater(horse, rider);
    }

    public static void breathe(LivingBreatheEvent event) {
        if (!(event.getEntity() instanceof Player player)
                || !(player.getVehicle() instanceof AbstractHorse horse)
                || !player.isEyeInFluid(FluidTags.WATER)) {
            return;
        }
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getBreed().isSkeleton()) {
            return;
        }
        if (BhHorseTraits.bondTier(data.bh_getBond()) >= 1 && BhAbility.SKELETON_DIVE.on()) {
            event.setCanBreathe(true);
            event.setRefillAirAmount(Math.max(event.getRefillAirAmount(), 4));
        } else if (!event.canBreathe() && BhAbility.SKELETON_BREATH.on()) {
            int drain = event.getConsumeAirAmount();
            event.setConsumeAirAmount(drain / 2 + ((player.tickCount & 1) == 0 ? drain % 2 : 0));
        }
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        dash.reset();
        data.bh_setSurge(0);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("cooldown", dash.saved());
    }

    @Override
    public void load(CompoundTag tag) {
        dash.load(tag.getInt("cooldown"));
    }
}
