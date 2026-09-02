package icy.betterhorses.net;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class BhHorseCombatAlert {

    private static final double RANGE_SQ = 256.0D;
    private static final int SPOOK_TICKS = 60;
    private static final double SAFE_DISMOUNT_DROP = 3.0D;

    private BhHorseCombatAlert() {}

    public static void rouse(ServerLevel level, Player owner, LivingEntity threat) {
        UUID ownerId = owner.getUUID();
        for (AbstractHorse horse : HorseTracker.getAll()) {
            if (horse.level() != level || horse.distanceToSqr(owner) > RANGE_SQ) {
                continue;
            }
            IHorseData data = IHorseData.of(horse);
            if (!ownerId.equals(data.bh_getOwner()) || !data.bh_getBreed().isRealBreed()) {
                continue;
            }
            if (horse.getControllingPassenger() == owner) {
                rollSpook(horse, data);
            } else if (!horse.isVehicle()) {
                defend(data, threat);
            }
        }
    }

    private static void rollSpook(AbstractHorse horse, IHorseData data) {
        if (data.bh_getSpookTicks() > 0) {
            return;
        }
        double chance = data.bh_getBreed().archetype()
                .spookChance(BhHorseTraits.bondTier(data.bh_getBond()));
        if (chance <= 0.0D || horse.getRandom().nextDouble() >= chance) {
            return;
        }
        if (!horse.onGround() || horse.fallDistance > SAFE_DISMOUNT_DROP) {
            return;
        }
        horse.ejectPassengers();
        data.bh_setSpookTicks(SPOOK_TICKS);
    }

    private static void defend(IHorseData data, LivingEntity threat) {
        if (BhHorseTraits.bondTier(data.bh_getBond()) < 1
                || data.bh_getCommand() == HorseCommand.STAY
                || data.bh_getCombatTarget() != null) {
            return;
        }
        data.bh_setCombatTarget(threat.getUUID());
    }
}
