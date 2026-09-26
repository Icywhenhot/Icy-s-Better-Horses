package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.registry.GenderType;
import net.minecraft.resources.ResourceKey;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class BreedingGameTest implements FabricGameTest {

    private static AbstractHorse ownedTamedHorse(GameTestHelper helper, HorseBreed breed, ResourceKey<GenderType> gender,
                                                  int x, int z) {
        AbstractHorse horse = helper.spawn(ModEntities.forBreed(BhBreeds.keyOf(breed)), x, 2, z);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(breed);
        data.bh_setGender(gender);
        data.bh_setOwner(UUID.randomUUID());
        data.bh_setBond(100);
        horse.setTamed(true);
        return horse;
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void differentBreedsOppositeGenderProduceMixedFoal(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        AbstractHorse mother = ownedTamedHorse(helper, HorseBreed.CLYDESDALE, BhContent.FEMALE.key(), 2, 2);
        AbstractHorse father = ownedTamedHorse(helper, HorseBreed.SHIRE, BhContent.MALE.key(), 3, 2);
        AABB nearby = mother.getBoundingBox().inflate(4);

        mother.spawnChildFromBreeding(level, father);

        List<AbstractHorse> foals = level.getEntitiesOfClass(AbstractHorse.class, nearby,
                h -> h != mother && h != father);
        helper.assertTrue(foals.size() == 1, "expected exactly one foal, found " + foals.size());
        AbstractHorse foal = foals.get(0);
        IHorseData foalData = IHorseData.of(foal);

        helper.assertTrue(foalData.bh_getBreed() == HorseBreed.CLYDESDALE || foalData.bh_getBreed() == HorseBreed.SHIRE,
                "foal breed should be one of the two parent breeds, was " + foalData.bh_getBreed());
        helper.assertTrue(foalData.bh_isMixedBreed(), "foal of two different real breeds should be flagged mixed");
        helper.assertTrue(foalData.bh_getGender() == BhContent.MALE.key() || foalData.bh_getGender() == BhContent.FEMALE.key(),
                "foal gender should be a sane value");
        helper.assertTrue(foal.getHealth() > 0 && foal.getHealth() <= foal.getMaxHealth(),
                "foal health should be positive and not exceed its max health");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void sameBreedParentsProduceUnmixedFoal(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        AbstractHorse mother = ownedTamedHorse(helper, HorseBreed.ARABIAN, BhContent.FEMALE.key(), 2, 2);
        AbstractHorse father = ownedTamedHorse(helper, HorseBreed.ARABIAN, BhContent.MALE.key(), 3, 2);
        AABB nearby = mother.getBoundingBox().inflate(4);

        mother.spawnChildFromBreeding(level, father);

        List<AbstractHorse> foals = level.getEntitiesOfClass(AbstractHorse.class, nearby,
                h -> h != mother && h != father);
        helper.assertTrue(foals.size() == 1, "expected exactly one foal, found " + foals.size());
        IHorseData foalData = IHorseData.of(foals.get(0));
        helper.assertTrue(foalData.bh_getBreed() == HorseBreed.ARABIAN, "foal of two Arabians should be an Arabian");
        helper.assertFalse(foalData.bh_isMixedBreed(), "foal of two same-breed parents should not be flagged mixed");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void sameGenderCannotMateWhenGenderBreedingOn(GameTestHelper helper) {
        floor(helper);
        helper.assertTrue(icy.betterhorses.net.BhFeature.GENDER_BREEDING.on(),
                "setup: gender_breeding should be on by default for this test");
        AbstractHorse a = ownedTamedHorse(helper, HorseBreed.MUSTANG, BhContent.MALE.key(), 2, 2);
        AbstractHorse b = ownedTamedHorse(helper, HorseBreed.MUSTANG, BhContent.MALE.key(), 3, 2);
        AbstractHorse c = ownedTamedHorse(helper, HorseBreed.MUSTANG, BhContent.FEMALE.key(), 4, 2);
        ServerPlayer feeder = helper.makeMockServerPlayerInLevel();
        for (AbstractHorse horse : List.of(a, b, c)) {
            horse.setInLove(feeder);
        }

        helper.assertFalse(a.canMate(b), "two males should not be able to mate when gender_breeding is on");
        helper.assertTrue(a.canMate(c), "opposite genders should be able to mate");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void breedingResetsParentsLoveCooldown(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        AbstractHorse mother = ownedTamedHorse(helper, HorseBreed.MORGAN, BhContent.FEMALE.key(), 2, 2);
        AbstractHorse father = ownedTamedHorse(helper, HorseBreed.MORGAN, BhContent.MALE.key(), 3, 2);
        ServerPlayer feeder = helper.makeMockServerPlayerInLevel();
        mother.setInLove(feeder);
        father.setInLove(feeder);
        helper.assertTrue(mother.isInLove() && father.isInLove(), "setup: both parents should start in love");

        mother.spawnChildFromBreeding(level, father);

        helper.assertFalse(mother.isInLove(), "breeding should reset the mother's love/cooldown state");
        helper.assertFalse(father.isInLove(), "breeding should reset the father's love/cooldown state");
        helper.succeed();
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 1, z, Blocks.STONE);
            }
        }
    }
}
