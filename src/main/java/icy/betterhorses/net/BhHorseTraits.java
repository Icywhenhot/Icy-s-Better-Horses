package icy.betterhorses.net;

import icy.betterhorses.net.tuning.Tune;
import icy.betterhorses.net.entity.BhBreedEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import icy.betterhorses.net.entity.BhBreedEntity;
import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.tuning.BondRules;
import icy.betterhorses.net.tuning.Tuning;

import java.util.List;

public final class BhHorseTraits {

    private BhHorseTraits() {}

    public static int bondTier(int bond) {
        return Tuning.bond().tier(bond);
    }

    private static final Tune MORGAN_RATE = Tune.of("easy_keeper", "bond_multiplier", 1.5D, 2.0D, 2.0D).within(0.0D, 10.0D);

    public static void grantBond(IHorseData data, int amount) {
        int gain = amount;
        int quick = BhAbility.MORGAN_BOND.tier(data, BhContent.MORGAN.getKey());
        double rate = quick >= 0 ? MORGAN_RATE.at(quick)
                : amount > 0 ? icy.betterhorses.net.feature.breed.WildTraits.bondRate(data) : 1.0D;
        if (quick >= 0 || rate != 1.0D) {
            int halves = amount * (int) Math.round(rate * 2.0D) + data.bh_getBondRemainder();
            gain = halves / 2;
            data.bh_setBondRemainder(halves % 2);
        }
        data.bh_setBond(data.bh_getBond() + gain);
    }

    public static void applyBondAttributes(AbstractHorse horse, int bond) {
        BondRules rules = Tuning.bond();
        BhHorseAttributes.apply(horse, Attributes.MOVEMENT_SPEED,
                BhHorseAttributes.Source.BOND, "growth",
                rules.speedBonus(bond), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        BhHorseAttributes.apply(horse, Attributes.JUMP_STRENGTH,
                BhHorseAttributes.Source.BOND, "growth",
                rules.jumpBonus(bond), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        float hp = horse.getHealth();
        BhHorseAttributes.apply(horse, Attributes.MAX_HEALTH,
                BhHorseAttributes.Source.BOND, "growth",
                rules.healthBonus(bond), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        if (horse.getHealth() > horse.getMaxHealth() || hp > horse.getMaxHealth()) {
            horse.setHealth(horse.getMaxHealth());
        }
    }

    public static HorseBreed pickBreed(AbstractHorse horse, RandomSource random) {
        if (horse instanceof BhBreedEntity breedEntity) {
            return HorseBreed.byId(breedEntity.bhFixedBreed().location().getPath());
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
