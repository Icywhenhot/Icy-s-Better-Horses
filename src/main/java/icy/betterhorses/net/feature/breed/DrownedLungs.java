package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;

public final class DrownedLungs implements BreedAbility {

    private final WaterDash dash = new WaterDash();

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide()) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        int packed = 0;
        if (dash.push(horse, rider, eligible(horse, data, rider))) {
            packed = BhSurge.pack(BhSurge.ACTIVE, dash.dash, WaterDash.DASH_TICKS, 0, 1);
        } else if (dash.cooldown > 0 && BhAbility.DROWNED_DASH.on()) {
            packed = BhSurge.pack(BhSurge.COOLING, dash.cooldown, WaterDash.COOLDOWN, 0, 1);
        } else if (rider != null && rider.isEyeInFluid(FluidTags.WATER) && BhAbility.DROWNED_GILLS.on()) {
            packed = BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 0);
        }
        BhSurge.set(data, BhContent.DROWNED_LUNGS.key(), packed);
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
        return data.bh_getBreed().isDrowned() && BhHorseTraits.bondTier(data.bh_getBond()) >= 2
                && BhAbility.DROWNED_DASH.on() && WaterDash.underwater(horse, rider);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        dash.reset();
        BhSurge.set(data, BhContent.DROWNED_LUNGS.key(), 0);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("cooldown", dash.saved());
    }

    @Override
    public void load(CompoundTag tag) {
        dash.load(tag.getIntOr("cooldown", 0));
    }

    public static int air(LivingEntity entity, int air, int left) {
        if (entity instanceof Player player && player.getVehicle() instanceof AbstractHorse horse
                && player.isEyeInFluid(FluidTags.WATER) && IHorseData.of(horse).bh_getBreed().isDrowned()
                && BhAbility.DROWNED_GILLS.on()) {
            return Math.min(player.getMaxAirSupply(), air + 4);
        }
        return left;
    }
}
