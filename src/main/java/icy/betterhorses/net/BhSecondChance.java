package icy.betterhorses.net;

import icy.betterhorses.net.feature.breed.SecondChance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public final class BhSecondChance {

    private BhSecondChance() {}

    public static boolean intercept(ServerLevel level, LivingEntity rider,
                                    DamageSource source, float amount) {
        if (amount < rider.getHealth() || source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (!(rider.getVehicle() instanceof AbstractHorse horse)) {
            return false;
        }
        IHorseData data = IHorseData.of(horse);
        if (data.bh_getBreed() != HorseBreed.ANDALUSIAN
                || BhHorseTraits.bondTier(data.bh_getBond()) < 2) {
            return false;
        }
        SecondChance ability = horse instanceof IHorseAbilityHost host
                && host.bh_currentAbility() instanceof SecondChance found ? found : null;
        if (ability == null || !ability.ready()) {
            return false;
        }

        ability.spend();
        BhSurge.pulse(data, 0);
        if (rider instanceof ServerPlayer saved) {
            BhCriteria.fire(saved, BhCriteria.SECOND_CHANCE);
        }
        horse.setHealth(1.0F);
        horse.hurtServer(level, source, 0.0F);
        rider.level().playSound(null, rider.getX(), rider.getY(), rider.getZ(),
                ModSounds.HORSE_NEIGH, rider.getSoundSource(), 1.0F, 0.7F);
        return true;
    }
}
