package icy.betterhorses.net;

import icy.betterhorses.net.feature.breed.ArchetypePerks;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class BhHorseCombatAlert {

    private static final double RANGE_SQ = 256.0D;
    private static final int SPOOK_TICKS = 60;
    private static final double SAFE_DISMOUNT_DROP = 3.0D;
    private static final double BLAST_RANGE = 16.0D;
    private static final double BLAST_SCARE = 10.0D;

    private BhHorseCombatAlert() {}

    public static void rouse(ServerLevel level, Player owner, LivingEntity threat) {
        UUID ownerId = owner.getUUID();
        for (AbstractHorse horse : HorseTracker.getAll()) {
            if (horse.level() != level || horse.distanceToSqr(owner) > RANGE_SQ) {
                continue;
            }
            IHorseData data = IHorseData.of(horse);
            if (!ownerId.equals(data.bh_getOwner()) || data.bh_getBreedKey() == null) {
                continue;
            }
            if (horse.getControllingPassenger() == owner) {
                if (BhFeature.HORSE_SPOOK.on()) {
                    rollSpook(horse, data, 1.0D);
                }
            } else if (!horse.isVehicle() && BhFeature.HORSE_DEFEND.on()) {
                defend(data, threat);
            }
        }
    }

    public static void startle(ServerLevel level, Vec3 at) {
        AABB area = AABB.ofSize(at, BLAST_RANGE * 2.0D, BLAST_RANGE * 2.0D, BLAST_RANGE * 2.0D);
        for (AbstractHorse horse : level.getEntitiesOfClass(AbstractHorse.class, area,
                h -> h.position().closerThan(at, BLAST_RANGE))) {
            IHorseData data = IHorseData.of(horse);
            if (data.bh_getBreedKey() == null) {
                continue;
            }
            if (!horse.isVehicle() && data.bh_isOwned()
                    && data.bh_getCommand().equals(BhContent.COMMAND_STAY.key())) {
                continue;
            }
            rollSpook(horse, data, BLAST_SCARE);
        }
    }

    private static void rollSpook(AbstractHorse horse, IHorseData data, double scare) {
        if (data.bh_getSpookTicks() > 0) {
            return;
        }
        double chance = scare * ArchetypePerks.spookChance(BhBreedData.of(data.bh_getBreedKey()).archetype(),
                BhHorseTraits.bondTier(data.bh_getBond()));
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
        if (!icy.betterhorses.net.feature.HorseCombat.mayTarget(threat)
                || BhHorseTraits.bondTier(data.bh_getBond()) < 1
                || data.bh_getCommand().equals(BhContent.COMMAND_STAY.key())
                || data.bh_getCombatTarget() != null) {
            return;
        }
        data.bh_setCombatTarget(threat.getUUID());
    }
}
