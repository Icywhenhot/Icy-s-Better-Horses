package icy.betterhorses.net.feature.breed;

import icy.betterhorses.net.BhAbility;
import icy.betterhorses.net.BhJumpHeight;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedDonkey;
import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.registry.BhContent;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Mule;

public final class MuleSire {

    private static final double SPEED_PER_RAW = 43.2D;

    private MuleSire() {}

    public static void apply(AbstractHorse a, AbstractHorse b, AbstractHorse child) {
        if (!(child instanceof Mule) || !(sire(a) || sire(b))) {
            return;
        }
        RandomSource random = child.getRandom();
        double speed = best(a, b, Attributes.MOVEMENT_SPEED) + (0.5D + random.nextDouble()) / SPEED_PER_RAW;
        double health = best(a, b, Attributes.MAX_HEALTH) + 2.0D + Math.round(random.nextDouble() * 2.0D);
        double jump = BhJumpHeight.strength(BhJumpHeight.blocks(best(a, b, Attributes.JUMP_STRENGTH))
                + 0.25D + random.nextDouble() * 0.25D);
        child.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Math.max(best(a, b, Attributes.MOVEMENT_SPEED), Math.min(speed, ArchetypeType.topSpeed())));
        child.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(best(a, b, Attributes.MAX_HEALTH), Math.min(health, ArchetypeType.topHealth())));
        child.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Math.max(best(a, b, Attributes.JUMP_STRENGTH), Math.min(jump, ArchetypeType.topJump())));
        child.setHealth(child.getMaxHealth());
    }

    private static boolean sire(AbstractHorse parent) {
        if (!(parent instanceof BhBreedDonkey donkey)
                || !IHorseData.of(donkey).bh_getGender().equals(BhContent.MALE.key())) {
            return false;
        }
        return donkey.bhFixedBreed().equals(BhContent.MAMMOTH_DONKEY.key()) && BhAbility.MAMMOTH_SIRE.on()
                || donkey.bhFixedBreed().equals(BhContent.PEGA_DONKEY.key()) && BhAbility.PEGA_SIRE.on();
    }

    private static double best(AbstractHorse a, AbstractHorse b, Holder<Attribute> attr) {
        return Math.max(a.getAttributeBaseValue(attr), b.getAttributeBaseValue(attr));
    }
}
