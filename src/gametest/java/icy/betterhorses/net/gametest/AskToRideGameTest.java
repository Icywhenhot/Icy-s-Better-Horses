package icy.betterhorses.net.gametest;

import com.mojang.authlib.GameProfile;
import icy.betterhorses.net.BhRideRequests;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Blocks;

import java.util.UUID;

// Ask-to-ride: a stranger refused mounting a horse triggers a chat prompt to the owner, rate
// limited per (owner, requester) pair by BhRideRequests. server.getPlayerList().getPlayer(owner)
// is how the gate tells whether the owner is online, so these tests use
// helper.makeMockServerPlayerInLevel()-style real, registered ServerPlayers for both sides
// (mockOnlinePlayer below, parameterized so owner/requester don't share a name and collide in
// GameProfileArgument lookups) rather than net.fabricmc.fabric.api.entity.FakePlayer: FakePlayer
// is a genuine ServerPlayer, but its constructor never calls PlayerList.placeNewPlayer, so
// getPlayerList().getPlayer(fakeUuid) still returns null and the owner would always look offline.
public class AskToRideGameTest implements FabricGameTest {

    private static ServerPlayer mockOnlinePlayer(GameTestHelper helper, String name) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), name));
        level.getServer().getPlayerList().placeNewPlayer(new Connection(PacketFlow.SERVERBOUND), player);
        return player;
    }

    private static AbstractHorse ownedHorse(GameTestHelper helper, UUID owner) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setTamed(true);
        IHorseData.of(horse).bh_setOwner(owner);
        return horse;
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void strangerRefusedAndRequestRecorded(GameTestHelper helper) {
        ServerPlayer owner = mockOnlinePlayer(helper, "AskOwnerA");
        ServerPlayer stranger = mockOnlinePlayer(helper, "AskRiderA");
        AbstractHorse horse = ownedHorse(helper, owner.getUUID());

        IHorseData.of(horse).bh_ridePlayer(stranger);
        helper.assertTrue(stranger.getVehicle() == null, "stranger must still be refused");

        BhRideRequests.Outcome again = BhRideRequests.instance().request(owner.getUUID(), stranger.getUUID());
        helper.assertTrue(again == BhRideRequests.Outcome.COOLDOWN,
                "the refused mount should have recorded a request, so a fresh ask right now cools down");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void secondAttemptInsideCooldownStillJustRefused(GameTestHelper helper) {
        ServerPlayer owner = mockOnlinePlayer(helper, "AskOwnerB");
        ServerPlayer stranger = mockOnlinePlayer(helper, "AskRiderB");
        AbstractHorse horse = ownedHorse(helper, owner.getUUID());

        IHorseData.of(horse).bh_ridePlayer(stranger);
        IHorseData.of(horse).bh_ridePlayer(stranger);

        helper.assertTrue(stranger.getVehicle() == null, "stranger must still be refused on the second try");
        BhRideRequests.Outcome outcome = BhRideRequests.instance().request(owner.getUUID(), stranger.getUUID());
        helper.assertTrue(outcome == BhRideRequests.Outcome.COOLDOWN,
                "still cooling down after a burst of attempts, not re-armed by the repeat");
        helper.succeed();
    }

    // BhCommands.deny() just calls BhRideRequests.mute(); exercised directly here rather than
    // through the literal "/horse deny" text. GameTestServer never initializes a GameProfileCache
    // (MinecraftServer.getProfileCache() is null there), and GameProfileArgument's plain-name
    // path needs one even for an online player, so dispatching that command crashes with an NPE
    // in this harness — a GameTest-environment limitation, not a production bug. A real server
    // always has a populated profile cache.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void afterDenyRequesterIsMuted(GameTestHelper helper) {
        ServerPlayer owner = mockOnlinePlayer(helper, "AskOwnerC");
        ServerPlayer stranger = mockOnlinePlayer(helper, "AskRiderC");
        AbstractHorse horse = ownedHorse(helper, owner.getUUID());

        BhRideRequests.instance().mute(owner.getUUID(), stranger.getUUID());

        BhRideRequests.Outcome outcome = BhRideRequests.instance().request(owner.getUUID(), stranger.getUUID());
        helper.assertTrue(outcome == BhRideRequests.Outcome.MUTED, "denied requester should be muted");

        IHorseData.of(horse).bh_ridePlayer(stranger);
        helper.assertTrue(stranger.getVehicle() == null, "muted requester is still just refused, no crash");
        helper.succeed();
    }

    // Same GameProfileCache-in-GameTest limitation as afterDenyRequesterIsMuted above: exercises
    // what BhCommands.trust() actually does (HorseTracker.trust + BhRideRequests.clear) rather
    // than dispatching the literal "/horse trust" text.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void afterTrustRequesterCanRide(GameTestHelper helper) {
        ServerPlayer owner = mockOnlinePlayer(helper, "AskOwnerD");
        ServerPlayer stranger = mockOnlinePlayer(helper, "AskRiderD");
        AbstractHorse horse = ownedHorse(helper, owner.getUUID());

        IHorseData.of(horse).bh_ridePlayer(stranger);
        helper.assertTrue(stranger.getVehicle() == null, "refused before trust");

        HorseTracker.trust(owner.getUUID(), stranger.getUUID(), "AskRiderD");
        BhRideRequests.instance().clear(owner.getUUID(), stranger.getUUID());

        IHorseData.of(horse).bh_ridePlayer(stranger);
        helper.assertTrue(stranger.getVehicle() == horse, "trusted requester should now be able to ride");

        BhRideRequests.Outcome outcome = BhRideRequests.instance().request(owner.getUUID(), stranger.getUUID());
        helper.assertTrue(outcome == BhRideRequests.Outcome.SEND,
                "trust should have cleared the earlier cooldown for this pair");
        helper.succeed();
    }
}
