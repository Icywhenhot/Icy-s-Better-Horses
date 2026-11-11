package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public final class SculkHush implements BreedAbility {

    private static final double LULL_RANGE = 48.0D;
    private static final int LULL_EVERY = 40;
    private static final int BROKEN_TICKS = 600;
    private static final String BROKEN_TAG = "BH_HushBroken";

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide() || horse.tickCount % LULL_EVERY != 0) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        if (rider == null || !lulls(data) || broken(horse)) {
            BhSurge.set(data, BhContent.SCULK_HUSH.getKey(), 0);
            return;
        }
        boolean near = !horse.level().getEntitiesOfClass(Warden.class, horse.getBoundingBox().inflate(LULL_RANGE)).isEmpty();
        BhSurge.set(data, BhContent.SCULK_HUSH.getKey(), near ? BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 1) : 0);
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
            return BhAbility.SCULK_KIN.on() && !broken(horse);
        }
        return entity instanceof Player player && player.getVehicle() instanceof AbstractHorse horse
                && horse.getControllingPassenger() == player && lulls(IHorseData.of(horse)) && !broken(horse);
    }

    public static void hit(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Warden)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        AbstractHorse horse = attacker instanceof AbstractHorse h ? h
                : attacker instanceof Player p && p.getVehicle() instanceof AbstractHorse h ? h : null;
        if (horse != null && IHorseData.of(horse).bh_getBreed().isSculk()) {
            horse.getPersistentData().putLong(BROKEN_TAG, horse.level().getGameTime() + BROKEN_TICKS);
        }
    }

    private static boolean broken(AbstractHorse horse) {
        return horse.getPersistentData().getLong(BROKEN_TAG) > horse.level().getGameTime();
    }
}
