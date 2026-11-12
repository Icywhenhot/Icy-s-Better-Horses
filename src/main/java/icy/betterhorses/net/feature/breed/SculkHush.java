package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.IHorseAbilityHost;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedAbilities;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;

public final class SculkHush implements BreedAbility {

    private static final double LULL_RANGE = 48.0D;
    private static final int LULL_EVERY = 40;
    private static final int BROKEN_TICKS = 600;

    private long brokenUntil;

    @Override
    public void tick(AbstractHorse horse, IHorseData data, BhAbilityState state) {
        if (horse.level().isClientSide() || horse.tickCount % LULL_EVERY != 0) {
            return;
        }
        Player rider = BhBreedAbilities.rider(horse);
        if (rider == null || !lulls(data) || broken(horse)) {
            BhSurge.set(data, BhContent.SCULK_HUSH.key(), 0);
            return;
        }
        boolean near = !horse.level().getEntitiesOfClass(Warden.class, horse.getBoundingBox().inflate(LULL_RANGE)).isEmpty();
        BhSurge.set(data, BhContent.SCULK_HUSH.key(), near ? BhSurge.pack(BhSurge.ACTIVE, 0, 0, 0, 1) : 0);
    }

    @Override
    public void onDetach(AbstractHorse horse, IHorseData data) {
        BhSurge.set(data, BhContent.SCULK_HUSH.key(), 0);
    }

    @Override
    public void save(CompoundTag tag) {
        if (brokenUntil > 0) tag.putLong("broken_until", brokenUntil);
    }

    @Override
    public void load(CompoundTag tag) {
        brokenUntil = tag.getLongOr("broken_until", 0L);
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

    public static void hit(LivingEntity target, DamageSource source) {
        if (!(target instanceof Warden)) {
            return;
        }
        Entity attacker = source.getEntity();
        AbstractHorse horse = attacker instanceof AbstractHorse h ? h
                : attacker instanceof Player p && p.getVehicle() instanceof AbstractHorse h ? h : null;
        SculkHush hush = horse == null ? null : of(horse);
        if (hush != null) {
            hush.brokenUntil = horse.level().getGameTime() + BROKEN_TICKS;
        }
    }

    private static boolean broken(AbstractHorse horse) {
        SculkHush hush = of(horse);
        return hush != null && hush.brokenUntil > horse.level().getGameTime();
    }

    private static SculkHush of(AbstractHorse horse) {
        if (horse instanceof IHorseAbilityHost host) {
            for (BreedAbility ability : host.bh_allAbilities()) {
                if (ability instanceof SculkHush hush) {
                    return hush;
                }
            }
        }
        return null;
    }
}
