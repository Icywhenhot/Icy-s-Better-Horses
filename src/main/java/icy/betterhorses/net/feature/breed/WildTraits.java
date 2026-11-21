package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhHorseTraits;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.tuning.Tune;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;

public final class WildTraits {

    private static final Tune MAX_TEMPER = Tune.of("wild", "max_temper", 150.0D).within(1.0D, 10000.0D);
    private static final Tune GIANT_TEMPER = Tune.of("wild", "giganteus_max_temper", 200.0D).within(1.0D, 10000.0D);
    private static final Tune BOND_RATE = Tune.of("wild", "bond_multiplier", 0.5D).within(0.0D, 10.0D);
    private static final Tune BUCK_EVERY = Tune.of("wild", "buck_check_ticks", 200.0D).within(1.0D, 72000.0D);
    private static final Tune BUCK_CHANCE = Tune.of("wild", "buck_chance", 0.1D, 0.04D, 0.0D).within(0.0D, 1.0D);
    public static final Tune WARY_RADIUS = Tune.of("wild", "wary_radius", 12.0D).within(0.0D, 64.0D);
    public static final Tune WEAPON_RADIUS = Tune.of("wild", "weapon_radius", 20.0D).within(0.0D, 64.0D);

    private static final double SAFE_DROP = 3.0D;

    private WildTraits() {}

    public static boolean wild(AbstractHorse horse) {
        return IHorseData.of(horse).bh_getBreed().isWild();
    }

    public static int maxTemper(AbstractHorse horse, int vanilla) {
        if (!BhAbility.WILD_TAMING.on() || !wild(horse)) {
            return vanilla;
        }
        return IHorseData.of(horse).bh_getBreed() == HorseBreed.GIGANTEUS ? GIANT_TEMPER.ticks() : MAX_TEMPER.ticks();
    }

    public static double bondRate(IHorseData data) {
        return BhAbility.WILD_BOND.on() && data.bh_getBreed().isWild() ? BOND_RATE.get() : 1.0D;
    }

    public static void tick(AbstractHorse horse, IHorseData data) {
        if (!BhAbility.WILD_BUCK.on() || horse.tickCount % BUCK_EVERY.ticks() != 0 || !horse.isTamed()
                || !(horse.getControllingPassenger() instanceof Player rider)) {
            return;
        }
        double chance = BUCK_CHANCE.at(BhHorseTraits.bondTier(data.bh_getBond()));
        if (chance <= 0.0D || horse.getRandom().nextDouble() >= chance
                || !horse.onGround() || horse.fallDistance > SAFE_DROP) {
            return;
        }
        horse.ejectPassengers();
        horse.makeMad();
        rider.displayClientMessage(Component.translatable("message.icys-better-horses.wild_buck", horse.getDisplayName()), true);
    }
}
