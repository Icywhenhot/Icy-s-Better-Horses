package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class HardyNorthern implements BreedAbility {

    private static final int SWEEP = 20;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        SlowBlockImmunity.show(data, SlowBlockImmunity.wading(horse)
                && (MuleLine.of(data) == null || SlowBlockImmunity.ignoresSlowBlocks(data)), 2);

        int freeze = BhAbility.ICELANDIC_FREEZE.tier(data);
        int clear = BhAbility.ICELANDIC_CLEAR.tier(data);
        if (freeze < 1 && clear < 1) {
            return;
        }

        Player rider = BhBreedAbilities.rider(horse);
        if (freeze >= 1) {
            boolean freezing = horse.getTicksFrozen() > 0;
            horse.setTicksFrozen(0);
            if (rider != null) {
                freezing |= rider.getTicksFrozen() > 0;
                rider.setTicksFrozen(0);
            }
            if (freezing) BhSurge.pulse(data, 0, 1);
        }

        if (clear < 2 || horse.tickCount % SWEEP != 0) {
            return;
        }
        boolean cleansed = purge(horse);
        if (rider != null) {
            cleansed |= purge(rider);
        }
        if (cleansed) {
            BhSurge.pulse(data, 0);
        }
    }

    private static boolean purge(LivingEntity target) {
        List<MobEffectInstance> bad = target.getActiveEffects().stream()
                .filter(e -> !e.getEffect().isBeneficial())
                .toList();
        for (MobEffectInstance effect : bad) {
            target.removeEffect(effect.getEffect());
        }
        return !bad.isEmpty();
    }

    public static boolean blocksFreeze(IHorseData data, int tier) {
        return MuleLine.of(data) != null ? BhAbility.ICELANDIC_FREEZE.tier(data) >= 1
                : Objects.equals(data.bh_getBreedKey(), BhContent.ICELANDIC.getKey()) && tier >= 1
                && BhAbility.ICELANDIC_FREEZE.on();
    }

    public static @Nullable AbstractHorse warden(LivingEntity target) {
        if (hardened(target)) {
            return (AbstractHorse) target;
        }
        return target.getVehicle() instanceof AbstractHorse horse && hardened(horse) ? horse : null;
    }

    private static boolean hardened(LivingEntity target) {
        return target instanceof AbstractHorse horse
                && BhAbility.ICELANDIC_CLEAR.tier(IHorseData.of(horse), BhContent.ICELANDIC.getKey()) >= 2;
    }
}
