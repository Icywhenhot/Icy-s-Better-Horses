package icy.betterhorses.net;

import icy.betterhorses.net.entity.BhBreedEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class BhHorseTraits {

    private static final Identifier SPEED_ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "bond_speed");
    private static final Identifier JUMP_ID =
            Identifier.fromNamespaceAndPath(IcysBetterHorses.MOD_ID, "bond_jump");

    private BhHorseTraits() {}

    public static void applyBondAttributes(AbstractHorse horse, int bond) {
        int bondLevel = Math.min(bond / 20, 5);
        double bonus = bondLevel * 0.15;

        AttributeInstance speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED_ID);
            if (bondLevel > 0) {
                speed.addTransientModifier(new AttributeModifier(
                        SPEED_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        AttributeInstance jump = horse.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            jump.removeModifier(JUMP_ID);
            if (bondLevel > 0) {
                jump.addTransientModifier(new AttributeModifier(
                        JUMP_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    public static HorseBreed pickBreed(AbstractHorse horse, RandomSource random) {
        if (horse instanceof BhBreedEntity breedEntity) {
            return breedEntity.bhFixedBreed();
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

    public static void blockSameGenderBreeding(AbstractHorse horse, IHorseData data, Player player) {
        if (!horse.isInLove()) {
            return;
        }
        HorseGender gender = data.bh_getGender();
        List<AbstractHorse> nearby = horse.level().getEntitiesOfClass(
                AbstractHorse.class,
                horse.getBoundingBox().inflate(8.0D),
                other -> other != horse && other.isInLove()
                        && IHorseData.of(other).bh_getGender() == gender);
        if (nearby.isEmpty()) {
            return;
        }
        horse.resetLove();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.icys-better-horses.same_gender_breed"));
        }
    }
}
