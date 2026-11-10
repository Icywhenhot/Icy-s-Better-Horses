package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class SculkHush implements BreedAbility {

    private static final double LULL_RANGE = 48.0D;
    private static final int LULL_EVERY = 10;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide() || horse.tickCount % LULL_EVERY != 0) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        if (rider == null || !lulls(data)) {
            BhSurge.set(data, BhContent.SCULK_HUSH.getKey(), 0);
            return;
        }
        List<Warden> wardens = horse.level().getEntitiesOfClass(Warden.class, horse.getBoundingBox().inflate(LULL_RANGE));
        for (Warden warden : wardens) {
            warden.clearAnger(rider);
            warden.clearAnger(horse);
            LivingEntity target = warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (target == rider || target == horse) {
                warden.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
        }
        BhSurge.set(data, BhContent.SCULK_HUSH.getKey(),
                wardens.isEmpty() ? 0 : BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 1));
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        BhSurge.set(data, BhContent.SCULK_HUSH.getKey(), 0);
    }

    private static boolean lulls(IHorseData data) {
        return data.bh_getBreed().isSculk() && BhHorseTraits.bondTier(data.bh_getBond()) >= 2
                && BhAbility.SCULK_LULL.on();
    }

    public static boolean hiddenFrom(Entity entity) {
        if (entity instanceof AbstractHorse horse && IHorseData.of(horse).bh_getBreed().isSculk()) {
            return BhAbility.SCULK_KIN.on();
        }
        return entity instanceof Player player && player.getVehicle() instanceof AbstractHorse horse
                && horse.getControllingPassenger() == player && lulls(IHorseData.of(horse));
    }
}
