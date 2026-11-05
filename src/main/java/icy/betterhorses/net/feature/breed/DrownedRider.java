package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;

public final class DrownedRider implements BreedAbility {

    private static final int COOLDOWN = 400;
    private static final int DASH_TICKS = 8;

    private int cooldown;
    private int dash;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (cooldown > 0) {
            cooldown--;
        }
        Player rider = BhBreedAbilities.rider(horse);
        int tier = BhHorseTraits.bondTier(data.bh_getBond());
        if (dash > 0 && eligible(horse, data, rider)) {
            Vec3 motion = rider.getLookAngle().scale(0.9D);
            horse.setDeltaMovement(motion);
            horse.hurtMarked = true;
            if (rider instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(horse));
            }
            dash--;
            data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, dash, DASH_TICKS, 0, 2));
        } else {
            dash = 0;
            if (cooldown > 0 && BhAbility.SKELETON_DASH.on()) {
                data.bh_setSurge(BhSurge.pack(BhSurge.COOLING, cooldown, COOLDOWN, 0, 2));
            } else if (rider != null && rider.isEyeInFluid(FluidTags.WATER)
                    && (tier >= 1 ? BhAbility.SKELETON_DIVE.on() : BhAbility.SKELETON_BREATH.on())) {
                data.bh_setSurge(BhSurge.pack(BhSurge.ACTIVE, 0, 0, tier >= 1 ? 0 : 50, tier >= 1 ? 1 : 0));
            } else {
                data.bh_setSurge(0);
            }
        }
    }

    @Override
    public boolean hasActiveSkill() {
        return true;
    }

    @Override
    public void onActivate(AbstractHorse horse, IHorseData data) {
        Player rider = BhBreedAbilities.rider(horse);
        if (horse.level().isClientSide() || cooldown > 0 || !eligible(horse, data, rider)) {
            return;
        }
        cooldown = COOLDOWN;
        dash = DASH_TICKS;
    }

    private static boolean eligible(AbstractHorse horse, IHorseData data, Player rider) {
        return rider != null && data.bh_getBreed().isSkeleton()
                && BhHorseTraits.bondTier(data.bh_getBond()) >= 2 && BhAbility.SKELETON_DASH.on()
                && horse.isInWater() && rider.isEyeInFluid(FluidTags.WATER);
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
        dash = 0;
        data.bh_setSurge(0);
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("cooldown", cooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        cooldown = Math.clamp(tag.getInt("cooldown"), 0, COOLDOWN);
        dash = 0;
    }
}
