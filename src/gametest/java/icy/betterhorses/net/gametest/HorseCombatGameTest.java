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

public class HorseCombatGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 60)
    public void chargeMeterReactsToKnownMovementWhileDeltaMovementIsZero(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true);
        horse.equipSaddle(null);

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
        horse.setDeltaMovement(Vec3.ZERO);
        horse.setPos(nextX, horse.getY(), horse.getZ());
        helper.runAfterDelay(1, () -> stepAndCheck(helper, horse, data, ticksLeft - 1, nextX + 0.35D, sawCharge));
    }
}
