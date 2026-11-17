package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.BhBreedData;
import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.entity.BhBreedMule;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Mule;

public final class MuleSire {

    private static final double SPEED_PER_RAW = 43.2D;

    private static final Tune SPEED_MIN = Tune.of("mule_sire", "speed_bonus_min", 0.5D).within(-100.0D, 100.0D);
    private static final Tune SPEED_MAX = Tune.of("mule_sire", "speed_bonus_max", 1.5D).within(-100.0D, 100.0D);
    private static final Tune HEALTH_MIN = Tune.of("mule_sire", "health_bonus_min", 2.0D).within(-1024.0D, 1024.0D);
    private static final Tune HEALTH_MAX = Tune.of("mule_sire", "health_bonus_max", 4.0D).within(-1024.0D, 1024.0D);
    private static final Tune JUMP_MIN = Tune.of("mule_sire", "jump_bonus_min", 0.25D).within(-10.0D, 10.0D);
    private static final Tune JUMP_MAX = Tune.of("mule_sire", "jump_bonus_max", 0.5D).within(-10.0D, 10.0D);

    private MuleSire() {}

    public static void apply(AbstractHorse a, AbstractHorse b, AbstractHorse child) {
        if (!(child instanceof Mule || child instanceof BhBreedMule) || !(sire(a) || sire(b))) {
            return;
        }
        RandomSource random = child.getRandom();
        double speed = best(a, b, Attributes.MOVEMENT_SPEED) + between(SPEED_MIN, SPEED_MAX, random.nextDouble()) / SPEED_PER_RAW;
        double health = best(a, b, Attributes.MAX_HEALTH)
                + Math.round(between(HEALTH_MIN, HEALTH_MAX, random.nextDouble()) - HEALTH_MIN.get()) + HEALTH_MIN.get();
        double jump = BhJumpHeight.strength(BhJumpHeight.blocks(best(a, b, Attributes.JUMP_STRENGTH))
                + between(JUMP_MIN, JUMP_MAX, random.nextDouble()));
        BhBreedData mule = child instanceof BhBreedMule bred ? BhBreedData.of(bred.bhFixedBreed()) : null;
        double topspeed = mule != null ? mule.speedRange()[1] : BhBreedData.topSpeed();
        double tophealth = mule != null ? mule.healthRange()[1] : BhBreedData.topHealth();
        double topjump = mule != null ? mule.jumpRange()[1] : BhBreedData.topJump();
        child.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Math.min(topspeed, Math.max(best(a, b, Attributes.MOVEMENT_SPEED), speed)));
        child.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(tophealth, Math.max(best(a, b, Attributes.MAX_HEALTH), health)));
        child.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Math.min(topjump, Math.max(best(a, b, Attributes.JUMP_STRENGTH), jump)));
        child.setHealth(child.getMaxHealth());
    }

    private static double between(Tune lo, Tune hi, double t) {
        double a = Math.min(lo.get(), hi.get());
        double b = Math.max(lo.get(), hi.get());
        return a + t * (b - a);
    }

    private static boolean sire(AbstractHorse parent) {
        if (!(parent instanceof BhBreedDonkey donkey)
                || !IHorseData.of(donkey).bh_getGender().equals(BhContent.MALE.getKey())) {
            return false;
        }
        return donkey.bhFixedBreed().equals(BhContent.MAMMOTH_DONKEY.getKey()) && BhAbility.MAMMOTH_SIRE.on()
                || donkey.bhFixedBreed().equals(BhContent.PEGA_DONKEY.getKey()) && BhAbility.PEGA_SIRE.on();
    }

    private static double best(AbstractHorse a, AbstractHorse b, Attribute attr) {
        return Math.max(a.getAttributeBaseValue(attr), b.getAttributeBaseValue(attr));
    }
}
