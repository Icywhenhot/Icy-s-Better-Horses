package icy.betterhorses.net.gametest;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.registry.BhContent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class MountStateGameTest implements FabricGameTest {

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

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void aHorseToldToStayFromTheSaddleStaysWhenItsOwnerGetsOff(GameTestHelper helper) {
        AbstractHorse horse = ownedHorse(helper);
        ServerPlayer owner = owner(helper, horse);
        owner.startRiding(horse, true);

        IcysBetterHorses.handleRadialCommand(owner, horse.getId(), BhContent.COMMAND_STAY.key());
        owner.stopRiding();

        boolean stays = IHorseData.of(horse).bh_getCommand().equals(BhContent.COMMAND_STAY.key());
        owner.discard();
        helper.assertTrue(stays, "a horse told to stay by its rider should stay once they get off, not wander");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void aHorseGivenNoOrderFromTheSaddleWandersWhenItsOwnerGetsOff(GameTestHelper helper) {
        AbstractHorse horse = ownedHorse(helper);
        ServerPlayer owner = owner(helper, horse);
        // an order from before it was mounted is not an order from the saddle
        IcysBetterHorses.handleRadialCommand(owner, horse.getId(), BhContent.COMMAND_STAY.key());
        owner.startRiding(horse, true);
        owner.stopRiding();

        boolean wanders = IHorseData.of(horse).bh_getCommand().equals(BhContent.COMMAND_WANDER.key());
        owner.discard();
        helper.assertTrue(wanders, "a horse its owner just gets off should be left to wander, as before");
        helper.succeed();
    }

    private static AbstractHorse ownedHorse(GameTestHelper helper) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(x, 1, z, Blocks.STONE);
            }
        }
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setTamed(true);
        return horse;
    }

    private static ServerPlayer owner(GameTestHelper helper, AbstractHorse horse) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new Vec3(2.5D, 2.0D, 3.5D)));
        IHorseData.of(horse).bh_setOwner(owner.getUUID());
        return owner;
    }
}
