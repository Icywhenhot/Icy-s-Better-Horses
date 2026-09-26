package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Blocks;

public class AdvancementsGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void breedingAwardsFoalAndMixedFoalAdvancements(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        ServerLevel level = helper.getLevel();
        AbstractHorse mother = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        AbstractHorse father = helper.spawn(ModEntities.forBreed(BhBreeds.keyOf(HorseBreed.SHIRE)), 3, 2, 2);
        IHorseData.of(mother).bh_setBreed(HorseBreed.CLYDESDALE);
        IHorseData.of(mother).bh_setGender(BhContent.FEMALE.key());
        IHorseData.of(father).bh_setBreed(HorseBreed.SHIRE);
        IHorseData.of(father).bh_setGender(BhContent.MALE.key());
        mother.setTamed(true);
        father.setTamed(true);

        ServerPlayer breeder = helper.makeMockServerPlayerInLevel();
        mother.setInLove(breeder);

        mother.spawnChildFromBreeding(level, father);

        helper.assertTrue(isDone(level.getServer(), breeder, "icys-better-horses:foal_play"),
                "breeding should award the foal_play advancement");
        helper.assertTrue(isDone(level.getServer(), breeder, "icys-better-horses:best_of_both_worlds"),
                "a mixed-breed foal should award the best_of_both_worlds advancement too");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void bondCrossingMaxAwardsRideOrDieAdvancement(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true);
        data.bh_setBond(90);

        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        data.bh_setOwner(owner.getUUID());

        helper.assertFalse(isDone(horse.level().getServer(), owner, "icys-better-horses:ride_or_die"),
                "setup: ride_or_die should not be earned before the bond crosses 100");

        data.bh_setBond(100);

        helper.assertTrue(isDone(horse.level().getServer(), owner, "icys-better-horses:ride_or_die"),
                "crossing max bond should award the ride_or_die advancement");
        helper.succeed();
    }

    private static boolean isDone(MinecraftServer server, ServerPlayer player, String advancementId) {
        Advancement advancement = server.getAdvancements().getAdvancement(new ResourceLocation(advancementId));
        if (advancement == null) return false;
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        return progress.isDone();
    }
}
