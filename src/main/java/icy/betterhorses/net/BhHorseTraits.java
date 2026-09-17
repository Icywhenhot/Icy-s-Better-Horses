package icy.betterhorses.net;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public final class BhHorseTraits {

    public static final int TIER_TWO_BOND = 40;
    public static final int TIER_THREE_BOND = 100;

    private BhHorseTraits() {}

    public static int bondTier(int bond) {
        if (bond >= TIER_THREE_BOND) return 2;
        if (bond >= TIER_TWO_BOND) return 1;
        return 0;
    }

    public static void grantBond(IHorseData data, int amount) {
        int gain = amount;
        if (data.bh_getBreed() == HorseBreed.MORGAN && BhAbility.MORGAN_BOND.on()) {
            int halves = amount * (bondTier(data.bh_getBond()) >= 1 ? 4 : 3) + data.bh_getBondRemainder();
            gain = halves / 2;
            data.bh_setBondRemainder(halves % 2);
        }
        data.bh_setBond(data.bh_getBond() + gain);
    }

    public static void applyBondAttributes(AbstractHorse horse, int bond) {
        double bonus = Math.min(bond / 20, 5) * 0.15D;
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.BOND, "growth",
                bonus, AttributeModifier.Operation.MULTIPLY_BASE);
        BhHorseAttributes.apply(horse, Attributes.JUMP_STRENGTH,
                BhHorseAttributes.Source.BOND, "growth",
                bonus, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}
