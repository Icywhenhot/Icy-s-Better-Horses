package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.registry.BhContent;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.UUID;

// Each test runs on its own raised runway (different heights) so the long lanes never cross
// another test's structure.
public class ReturnHomeGameTest implements FabricGameTest {

    private static final double SPEED = 0.225D;
    // blocks per tick a ridden horse reaches at full gallop, per point of movement speed
    private static final double GALLOP_PER_SPEED = 43.17D / 20.0D;
    private static final int LANE = 76;
    private static final TicketType<ChunkPos> LANE_TICKET =
            TicketType.create("bh_test_lane", Comparator.comparingLong(ChunkPos::toLong), 400);

    // Home is far down the runway, and the test reads its pace before the first leg ends in a teleport.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
    public void runsHomeAtAPaceAnotherHorseCanKeepUpWith(GameTestHelper helper) {
        int y = 150;
        keepLaneLoaded(helper);
        floor(helper, y);
        AbstractHorse horse = runner(helper, y);
        IHorseData data = IHorseData.of(horse);
        data.bh_setOwner(UUID.randomUUID());
        data.bh_setHome(helper.absolutePos(new BlockPos(62, y + 1, 4)));
        // sent once it has landed: a horse still in the air cannot plan a path
        helper.runAfterDelay(10, () -> data.bh_setCommand(BhContent.COMMAND_RETURN_HOME.key()));

        // one second in the middle of the run: past the start-up, before the first leg runs out
        double[] at = new double[1];
        helper.runAfterDelay(40, () -> at[0] = horse.getX());
        helper.runAfterDelay(60, () -> {
            double covered = horse.getX() - at[0];
            double gallopCovers = horse.getAttributeValue(Attributes.MOVEMENT_SPEED) * GALLOP_PER_SPEED * 20.0D;
            horse.discard();
            helper.assertTrue(covered >= gallopCovers * 0.65D && covered <= gallopCovers * 0.85D,
                    "a horse sent home should run there a little under its gallop: covered "
                            + String.format("%.1f", covered) + " blocks in 1 second, a gallop covers "
                            + String.format("%.1f", gallopCovers));
            helper.succeed();
        });
    }

    private static AbstractHorse runner(GameTestHelper helper, int floorY) {
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, floorY + 1, 4);
        IHorseData.of(horse).bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true);
        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(SPEED);
        return horse;
    }

    private static void floor(GameTestHelper helper, int y) {
        for (int x = 0; x <= 70; x++) {
            for (int z = 1; z <= 7; z++) {
                helper.setBlock(x, y, z, Blocks.STONE);
            }
        }
    }

    // The runways reach well past the test structure, into chunks the test server never loads itself.
    // The tickets expire on their own, so tests sharing a chunk cannot unload it under each other.
    private static void keepLaneLoaded(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (int cx = (origin.getX() - 16) >> 4; cx <= (origin.getX() + LANE + 16) >> 4; cx++) {
            for (int cz = (origin.getZ() - 16) >> 4; cz <= (origin.getZ() + 48) >> 4; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                level.getChunkSource().addRegionTicket(LANE_TICKET, chunk, 2, chunk);
            }
        }
    }
}
