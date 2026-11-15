package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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

    public static int air(LivingEntity entity, int air, int left) {
        if (!(entity instanceof Player player)
                || !(player.getVehicle() instanceof AbstractHorse horse)
                || !player.isEyeInFluid(FluidTags.WATER)) {
            return left;
        }
        IHorseData data = IHorseData.of(horse);
        if (!data.bh_getBreed().isSkeleton()) {
            return left;
        }
        if (BhHorseTraits.bondTier(data.bh_getBond()) >= 1 && BhAbility.SKELETON_DIVE.on()) {
            return Math.min(player.getMaxAirSupply(), air + 4);
        }
        if (BhAbility.SKELETON_BREATH.on()) {
            int drain = air - left;
            return air - (drain / 2 + ((player.tickCount & 1) == 0 ? drain % 2 : 0));
        }
        return left;
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
        dash.load(tag.getIntOr("cooldown", 0));
    }
}
