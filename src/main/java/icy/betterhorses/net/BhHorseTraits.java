package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;
import icy.betterhorses.net.registry.BhContent;

import java.util.List;
import java.util.Objects;

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
        if (Objects.equals(data.bh_getBreedKey(), BhContent.MORGAN.key()) && BhAbility.MORGAN_BOND.on()) {
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
                bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        BhHorseAttributes.apply(horse, Attributes.JUMP_STRENGTH,
                BhHorseAttributes.Source.BOND, "growth",
                bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public static HorseBreed pickBreed(AbstractHorse horse, RandomSource random) {
        if (horse instanceof BhBreedEntity breedEntity) {
            return HorseBreed.byId(breedEntity.bhFixedBreed().identifier().getPath());
        }
        HorseBreed species = HorseBreed.speciesFor(horse);
        if (species != null) {
            return species;
        }
        if (horse instanceof Horse plainHorse) {
            List<HorseBreed> matches =
                    HorseBreed.breedsMatchingCoat(plainHorse.getVariant(), plainHorse.getMarkings());
            if (!matches.isEmpty()) {
                return matches.get(random.nextInt(matches.size()));
            }
        }
        return HorseBreed.MUSTANG;
    }

}
