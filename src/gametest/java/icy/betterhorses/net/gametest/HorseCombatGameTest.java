package icy.betterhorses.net.gametest;

import icy.betterhorses.net.BhSurge;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

// Round 2, item 2: the server zeroes a player-ridden horse's getDeltaMovement() (see
// LivingEntity.travelRidden), so charge/bash logic must read bh_getKnownMovement() (a real
// position delta recorded in bh_tick) instead.
public class HorseCombatGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 60)
    public void chargeMeterReactsToKnownMovementWhileDeltaMovementIsZero(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true); // horse left unowned so RiderGate doesn't eject the mock rider each tick
        horse.equipSaddle(null); // getControllingPassenger() only returns a rider once saddled

        Player rider = helper.makeMockPlayer();
        helper.assertTrue(rider.startRiding(horse, true) && horse.getControllingPassenger() == rider,
                "setup: rider should be the controlling passenger");

        boolean[] sawCharge = {false};
        helper.runAfterDelay(2, () -> stepAndCheck(helper, horse, data, 8, horse.getX(), sawCharge));
    }

    private void stepAndCheck(GameTestHelper helper, AbstractHorse horse, IHorseData data,
                               int ticksLeft, double nextX, boolean[] sawCharge) {
        if (data.bh_getCharge() > BhSurge.HIDDEN) {
            sawCharge[0] = true;
        }
        if (ticksLeft <= 0) {
            helper.assertTrue(sawCharge[0], "expected the charge meter to react to the horse's real "
                    + "movement even though getDeltaMovement() stayed zero the whole time (server-ridden horse)");
            helper.succeed();
            return;
        }
        // Simulate what the server does for a ridden horse: getDeltaMovement() stays zero while the
        // horse still visibly moves (position updates come from elsewhere in that codepath).
        horse.setDeltaMovement(Vec3.ZERO);
        horse.setPos(nextX, horse.getY(), horse.getZ());
        helper.runAfterDelay(1, () -> stepAndCheck(helper, horse, data, ticksLeft - 1, nextX + 0.35D, sawCharge));
    }
}
