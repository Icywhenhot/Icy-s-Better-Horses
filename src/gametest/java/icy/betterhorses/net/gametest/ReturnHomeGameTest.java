package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ReturnHomeGameTest implements FabricGameTest {

    // F3 (5dbf2c4): with teleporting off, RETURN_HOME must give up (STAY) instead of retrying
    // forever once home is unreachable. The home cell is boxed in with barrier walls, floor open.
    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 400)
    public void givesUpWhenHomeIsUnreachable(GameTestHelper helper) {
        BlockPos home = new BlockPos(6, 2, 6);
        floor(helper);
        enclose(helper, home);

        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 1, 2, 1);
        IHorseData data = IHorseData.of(horse);
        data.bh_setOwner(UUID.randomUUID());
        data.bh_setHome(helper.absolutePos(home));
        data.bh_setCommand(HorseCommand.RETURN_HOME);

        helper.succeedWhen(() -> helper.assertTrue(data.bh_getCommand() == HorseCommand.STAY,
                "RETURN_HOME should give up (STAY) after repeated stuck/failed-path checks with teleport off"));
    }

    // With a reachable home a few blocks away and teleport off, the horse must walk there, not
    // teleport - the only way home can be reached at all with HORSE_TELEPORT off is on foot.
    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 300)
    public void walksHomeWhenReachable(GameTestHelper helper) {
        BlockPos home = new BlockPos(6, 2, 1);
        floor(helper);

        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 1, 2, 1);
        IHorseData data = IHorseData.of(horse);
        data.bh_setOwner(UUID.randomUUID());
        data.bh_setHome(helper.absolutePos(home));
        data.bh_setCommand(HorseCommand.RETURN_HOME);

        Vec3 homeCenter = Vec3.atBottomCenterOf(helper.absolutePos(home));
        helper.succeedWhen(() -> {
            helper.assertTrue(data.bh_getCommand() == HorseCommand.STAY, "horse should have arrived and stopped");
            helper.assertTrue(horse.distanceToSqr(homeCenter) <= 4.0, "horse should have walked to within range of home");
        });
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 1, z, Blocks.STONE);
            }
        }
    }

    // Seals every horizontal neighbor of home at both horse-height levels, leaving home itself open.
    private static void enclose(GameTestHelper helper, BlockPos home) {
        int[] dx = {1, -1, 0, 0, 1, 1, -1, -1};
        int[] dz = {0, 0, 1, -1, 1, -1, 1, -1};
        for (int i = 0; i < dx.length; i++) {
            for (int y = 2; y <= 3; y++) {
                helper.setBlock(home.getX() + dx[i], y, home.getZ() + dz[i], Blocks.BARRIER);
            }
        }
    }
}
