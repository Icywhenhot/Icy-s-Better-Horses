package icy.betterhorses.net.gametest;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class MountStateGameTest implements FabricGameTest {

    // F6 (72a5fe3): the HEAD-cancelling doPlayerRide mixin skips vanilla's own clear of eating,
    // so mounting must clear it itself or the horse stays immobile (grazing) after mount.
    // (Eating and standing/rearing are mutually exclusive in vanilla, so these are two tests.)
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void mountingClearsGrazing(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setEating(true);
        helper.assertTrue(horse.isEating(), "setup: horse should start grazing");

        Player player = helper.makeMockPlayer();
        IHorseData.of(horse).bh_ridePlayer(player);

        helper.assertTrue(player.getVehicle() == horse, "mock player should now be riding the horse");
        helper.assertFalse(horse.isEating(), "mounting should clear grazing");
        helper.succeed();
    }

    // Same fix, the standing/rearing half: mounting must also clear rearing itself.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void mountingClearsRearing(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setStanding(true);
        helper.assertTrue(horse.isStanding(), "setup: horse should start rearing");

        Player player = helper.makeMockPlayer();
        IHorseData.of(horse).bh_ridePlayer(player);

        helper.assertTrue(player.getVehicle() == horse, "mock player should now be riding the horse");
        helper.assertFalse(horse.isStanding(), "mounting should clear rearing");
        helper.succeed();
    }
}
