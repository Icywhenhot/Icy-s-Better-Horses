package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseManageAction;
import icy.betterhorses.net.HorseManagement;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class HandlerSecurityGameTest implements FabricGameTest {

    private static AbstractHorse ownedHorse(GameTestHelper helper, UUID owner) {
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setOwner(owner);
        data.bh_setBond(100);
        horse.setTamed(true);
        return horse;
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandOwnerChangesState(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), ownerId);

        IcysBetterHorses.handleRadialCommand(owner, horse.getId(), BhContent.COMMAND_WANDER.key());

        helper.assertTrue(IHorseData.of(horse).bh_getCommand() == BhContent.COMMAND_WANDER.key(),
                "owner's radial command should change the horse's command");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandStrangerLeavesStateUnchanged(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        IHorseData.of(horse).bh_setCommand(BhContent.COMMAND_FOLLOW.key());
        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 3));

        IcysBetterHorses.handleRadialCommand(stranger, horse.getId(), BhContent.COMMAND_WANDER.key());

        helper.assertTrue(IHorseData.of(horse).bh_getCommand() == BhContent.COMMAND_FOLLOW.key(),
                "a stranger's radial command must not change the horse's command");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandTrustedPlayerAllowed(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        UUID trustedId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        IHorseData.of(horse).bh_setCommand(BhContent.COMMAND_FOLLOW.key());
        HorseTracker.trust(ownerId, trustedId, "trusted-friend");
        ServerPlayer trusted = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), trustedId);

        IcysBetterHorses.handleRadialCommand(trusted, horse.getId(), BhContent.COMMAND_WANDER.key());

        helper.assertTrue(IHorseData.of(horse).bh_getCommand() == BhContent.COMMAND_WANDER.key(),
                "a trusted player should be able to issue radial commands");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandFarAwayIgnored(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        IHorseData.of(horse).bh_setCommand(BhContent.COMMAND_FOLLOW.key());
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 40), ownerId);

        IcysBetterHorses.handleRadialCommand(owner, horse.getId(), BhContent.COMMAND_WANDER.key());

        helper.assertTrue(IHorseData.of(horse).bh_getCommand() == BhContent.COMMAND_FOLLOW.key(),
                "a radial command from far away should be ignored");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandUntamedHorseIgnored(GameTestHelper helper) {
        Horse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        ServerPlayer player = BhTestPlayers.at(helper, new Vec3(2, 2, 3));

        IcysBetterHorses.handleRadialCommand(player, horse.getId(), BhContent.COMMAND_WANDER.key());

        helper.assertTrue(IHorseData.of(horse).bh_getCommand() != BhContent.COMMAND_WANDER.key(),
                "an untamed horse should ignore radial commands");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandBogusEntityIdDoesNotThrow(GameTestHelper helper) {
        ServerPlayer player = BhTestPlayers.at(helper, new Vec3(2, 2, 2));
        IcysBetterHorses.handleRadialCommand(player, 987654, BhContent.COMMAND_STAY.key());
        IcysBetterHorses.handleRear(player, 987654);
        IcysBetterHorses.handleGearShift(player, 987654, 1, 1);
        IcysBetterHorses.handleFreeLook(player, 987654, true);
        IcysBetterHorses.handleCartSize(player, 987654);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandRemovedHorseDoesNotThrow(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        int id = horse.getId();
        horse.discard();
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), ownerId);

        IcysBetterHorses.handleRadialCommand(owner, id, BhContent.COMMAND_WANDER.key());
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void fromIdClampsOutOfRangeOrdinals(GameTestHelper helper) {
        helper.assertTrue(HorseManageAction.fromId(-5) == HorseManageAction.WHISTLE, "negative action id should clamp low");
        helper.assertTrue(HorseManageAction.fromId(9999) == HorseManageAction.SET_ACTIVE, "huge action id should clamp high");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void rearOwnerNotRidingStillWorks(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), ownerId);

        helper.runAfterDelay(10, () -> {
            IcysBetterHorses.handleRear(owner, horse.getId());
            helper.assertTrue(horse.isStanding(), "owner should be able to make an unridden owned horse rear");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void rearStrangerDenied(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 3));

        IcysBetterHorses.handleRear(stranger, horse.getId());

        helper.assertFalse(horse.isStanding(), "a stranger should not be able to make an owned horse rear");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void rearBlockedWhileSomeoneElseRides(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        horse.equipSaddle(null);
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), ownerId);
        ServerPlayer rider = helper.makeMockServerPlayerInLevel();
        boolean mounted = rider.startRiding(horse, true);
        helper.assertTrue(mounted && horse.getControllingPassenger() == rider,
                "setup: rider should be the controlling passenger");

        IcysBetterHorses.handleRear(owner, horse.getId());

        helper.assertFalse(horse.isStanding(),
                "the owner should not be able to force a rear while someone else is riding");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void gearShiftOnlyControllingPassenger(GameTestHelper helper) {
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.equipSaddle(null);
        ServerPlayer rider = helper.makeMockServerPlayerInLevel();
        ServerPlayer bystander = helper.makeMockServerPlayerInLevel();

        IcysBetterHorses.handleGearShift(bystander, horse.getId(), 3, 2);
        helper.assertTrue(IHorseData.of(horse).bh_getGear() != 3,
                "a non-passenger should not be able to shift gear");

        rider.startRiding(horse, true);
        helper.assertTrue(horse.getControllingPassenger() == rider, "setup: rider should be the controlling passenger");
        IcysBetterHorses.handleGearShift(rider, horse.getId(), 3, 2);
        helper.assertTrue(IHorseData.of(horse).bh_getGear() == 3,
                "the controlling passenger should be able to shift gear");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void freeLookOnlyControllingPassenger(GameTestHelper helper) {
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.equipSaddle(null);
        ServerPlayer bystander = helper.makeMockServerPlayerInLevel();

        IcysBetterHorses.handleFreeLook(bystander, horse.getId(), true);
        helper.assertFalse(IHorseData.of(horse).bh_isFreeLook(),
                "a non-passenger should not be able to toggle free look");

        ServerPlayer rider = helper.makeMockServerPlayerInLevel();
        rider.startRiding(horse, true);
        IcysBetterHorses.handleFreeLook(rider, horse.getId(), true);
        helper.assertTrue(IHorseData.of(horse).bh_isFreeLook(),
                "the controlling passenger should be able to toggle free look");
        helper.succeed();
    }

    private static AbstractHorse ownedCartHorse(GameTestHelper helper, UUID owner) {
        AbstractHorse horse = ownedHorse(helper, owner);
        IHorseData.of(horse).bh_getGearContainer()
                .setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_CART));
        return horse;
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void cartSizeOwnerCanResize(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedCartHorse(helper, ownerId);
        helper.assertTrue(IHorseData.of(horse).bh_hasCartGear(), "setup: horse should carry cart gear");
        boolean before = IHorseData.of(horse).bh_hasLargeCart();
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), ownerId);

        IcysBetterHorses.handleCartSize(owner, horse.getId());

        helper.assertTrue(IHorseData.of(horse).bh_hasLargeCart() != before,
                "owner should be able to resize their own cart");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void cartSizeStrangerDenied(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedCartHorse(helper, ownerId);
        boolean before = IHorseData.of(horse).bh_hasLargeCart();
        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 3));

        IcysBetterHorses.handleCartSize(stranger, horse.getId());

        helper.assertTrue(IHorseData.of(horse).bh_hasLargeCart() == before,
                "a stranger should not be able to resize someone else's cart");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void cartSizeTrustedCanResize(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        UUID trustedId = UUID.randomUUID();
        AbstractHorse horse = ownedCartHorse(helper, ownerId);
        boolean before = IHorseData.of(horse).bh_hasLargeCart();
        HorseTracker.trust(ownerId, trustedId, "trusted-friend");
        ServerPlayer trusted = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), trustedId);

        IcysBetterHorses.handleCartSize(trusted, horse.getId());

        helper.assertTrue(IHorseData.of(horse).bh_hasLargeCart() != before,
                "a trusted player should be able to resize the cart (trust grants handling)");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void cartSizeFarAwayIgnored(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedCartHorse(helper, ownerId);
        boolean before = IHorseData.of(horse).bh_hasLargeCart();
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 40), ownerId);

        IcysBetterHorses.handleCartSize(owner, horse.getId());

        helper.assertTrue(IHorseData.of(horse).bh_hasLargeCart() == before,
                "a cart size change from out of reach should be ignored");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void manageWhistleOwnerOnly(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        UUID trustedId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        HorseTracker.register(horse);
        HorseTracker.trust(ownerId, trustedId, "trusted-friend");

        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 6));
        HorseManagement.Outcome strangerResult = HorseManagement.whistle(stranger, horse.getUUID());
        helper.assertFalse(strangerResult.ok(), "a stranger should not be able to whistle someone else's horse");

        ServerPlayer trusted = BhTestPlayers.owner(helper, new Vec3(2, 2, 6), trustedId);
        HorseManagement.Outcome trustedResult = HorseManagement.whistle(trusted, horse.getUUID());
        helper.assertFalse(trustedResult.ok(), "whistling/management is owner-only, trust should not grant it");

        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 6), ownerId);
        HorseManagement.Outcome ownerResult = HorseManagement.whistle(owner, horse.getUUID());
        helper.assertTrue(ownerResult.ok(), "the owner should be able to whistle their own horse");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void manageSendHomeStrangerDenied(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        IHorseData.of(horse).bh_setHome(horse.blockPosition());
        HorseTracker.register(horse);
        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 6));

        HorseManagement.Outcome result = HorseManagement.sendHome(stranger, horse.getUUID());

        helper.assertFalse(result.ok(), "a stranger should not be able to send someone else's horse home");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void manageDisownStrangerDenied(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        HorseTracker.register(horse);
        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 6));

        HorseManagement.Outcome result = HorseManagement.disown(stranger, horse.getUUID());

        helper.assertFalse(result.ok(), "a stranger should not be able to disown someone else's horse");
        helper.assertTrue(IHorseData.of(horse).bh_getOwner() != null && horse.isTamed(),
                "a denied disown must leave the horse owned and tamed");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void manageSetActiveStrangerDenied(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        HorseTracker.register(horse);
        ServerPlayer stranger = BhTestPlayers.at(helper, new Vec3(2, 2, 6));

        HorseManagement.Outcome result = HorseManagement.setActive(stranger, horse.getUUID());

        helper.assertFalse(result.ok(), "a stranger should not be able to set someone else's horse active");
        helper.assertTrue(HorseTracker.getActiveHorseId(stranger.getUUID()) == null,
                "a denied set-active must not register the horse as the stranger's active horse");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void manageActionsOnUnknownHorseIdDoNotThrow(GameTestHelper helper) {
        ServerPlayer player = BhTestPlayers.at(helper, new Vec3(2, 2, 2));
        UUID bogus = UUID.randomUUID();
        for (HorseManageAction action : HorseManageAction.values()) {
            IcysBetterHorses.handleManageAction(player, bogus, action);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void whistleOtherDimensionFailsCleanly(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        AbstractHorse horse = ownedHorse(helper, ownerId);
        HorseTracker.register(horse);
        ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        helper.assertTrue(nether != null, "setup: nether level should exist");
        ServerPlayer owner = BhTestPlayers.atLevel(nether, ownerId,
                new Vec3(nether.getSharedSpawnPos().getX(), nether.getSharedSpawnPos().getY(),
                        nether.getSharedSpawnPos().getZ()));

        HorseManagement.Outcome result = HorseManagement.whistle(owner, horse.getUUID());

        helper.assertFalse(result.ok(), "whistling a horse loaded in a different dimension should fail, not throw");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void callHorseWithNoHorsesDoesNotThrow(GameTestHelper helper) {
        ServerPlayer player = BhTestPlayers.at(helper, new Vec3(2, 2, 2));
        IcysBetterHorses.handleCallHorse(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void recallClearsOnlyOwnHorsesCombat(GameTestHelper helper) {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        AbstractHorse mine = ownedHorse(helper, ownerId);
        AbstractHorse theirs = helper.spawn(ModEntities.CLYDESDALE_HORSE, 4, 2, 4);
        IHorseData.of(theirs).bh_setOwner(otherId);
        IHorseData.of(mine).bh_setCombatTarget(UUID.randomUUID());
        IHorseData.of(theirs).bh_setCombatTarget(UUID.randomUUID());
        HorseTracker.register(mine);
        HorseTracker.register(theirs);
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 2), ownerId);

        IcysBetterHorses.handleRecall(owner);

        helper.assertTrue(IHorseData.of(mine).bh_getCombatTarget() == null,
                "recall should clear the caller's own horse's combat target");
        helper.assertTrue(IHorseData.of(theirs).bh_getCombatTarget() != null,
                "recall must not touch another player's horse");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void rosterOnlyListsOwnHorses(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        AbstractHorse mine = ownedHorse(helper, ownerId);
        AbstractHorse theirs = helper.spawn(ModEntities.CLYDESDALE_HORSE, 4, 2, 4);
        IHorseData.of(theirs).bh_setOwner(otherId);
        HorseTracker.register(mine);
        HorseTracker.register(theirs);
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 2), ownerId);

        var roster = HorseManagement.buildRoster(owner);

        helper.assertTrue(roster.size() == 1, "roster should contain exactly the caller's own horse");
        helper.assertTrue(roster.get(0).horseId().equals(mine.getUUID()), "roster entry should be the owner's horse");
        IcysBetterHorses.sendRoster(owner);
        helper.succeed();
    }
}
