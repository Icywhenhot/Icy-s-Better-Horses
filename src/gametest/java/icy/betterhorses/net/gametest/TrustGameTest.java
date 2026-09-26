package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class TrustGameTest implements FabricGameTest {

    private static AbstractHorse ownedHorse(GameTestHelper helper, UUID owner) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setTamed(true);
        IHorseData.of(horse).bh_setOwner(owner);
        return horse;
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void ownerCanRide(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer();
        AbstractHorse horse = ownedHorse(helper, owner.getUUID());
        IHorseData.of(horse).bh_ridePlayer(owner);
        helper.assertTrue(owner.getVehicle() == horse, "owner should be riding");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void trustedPlayerCanRideWithoutTakingTheWhistle(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, owner);
        Player friend = helper.makeMockPlayer();
        helper.assertTrue(HorseTracker.trust(owner, friend.getUUID(), "friend"), "setup: trust should succeed");

        IHorseData.of(horse).bh_ridePlayer(friend);
        helper.assertTrue(friend.getVehicle() == horse, "trusted player should be riding");
        helper.assertTrue(HorseTracker.getActiveHorseId(friend.getUUID()) == null,
                "riding a friend's horse must not make it your active horse");
        helper.assertTrue(IHorseData.of(horse).bh_getOwner().equals(owner), "owner must not change");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void strangerCannotRide(GameTestHelper helper) {
        AbstractHorse horse = ownedHorse(helper, UUID.randomUUID());
        Player stranger = helper.makeMockPlayer();
        IHorseData.of(horse).bh_ridePlayer(stranger);
        helper.assertTrue(stranger.getVehicle() == null, "stranger must be refused");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void untrustedPlayerIsRefusedAgain(GameTestHelper helper) {
        UUID owner = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, owner);
        Player friend = helper.makeMockPlayer();
        HorseTracker.trust(owner, friend.getUUID(), "friend");
        HorseTracker.untrust(owner, friend.getUUID());
        IHorseData.of(horse).bh_ridePlayer(friend);
        helper.assertTrue(friend.getVehicle() == null, "untrusted player must be refused");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void trustedPlayerCanRidePillionBehindOwner(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer();
        AbstractHorse horse = ownedHorse(helper, owner.getUUID());
        Player friend = helper.makeMockPlayer();
        Player stranger = helper.makeMockPlayer();
        IHorseData.of(horse).bh_ridePlayer(owner);
        IHorseData.of(horse).bh_ridePlayer(stranger);
        helper.assertTrue(owner.getVehicle() == horse, "owner should be riding");
        helper.assertTrue(stranger.getVehicle() == horse,
                "a second rider behind the owner is allowed (unchanged behaviour)");
        stranger.stopRiding();
        HorseTracker.trust(owner.getUUID(), friend.getUUID(), "friend");
        IHorseData.of(horse).bh_ridePlayer(friend);
        helper.assertTrue(friend.getVehicle() == horse, "trusted pillion rider should be seated");
        helper.succeed();
    }
}
