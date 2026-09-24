package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// Round 2, item 2: every mod breed x every HorseCommand, owned + bonded, ticked through the whole
// cycle. Not a correctness test - just breadth to shake out crashes/NaNs/dupes/despawns, in both
// the default batch and Jake's server batch (teleport off, convert-tamed off).
public class BreedCommandSmokeGameTest implements FabricGameTest {

    private static final int TICKS_PER_COMMAND = 15;

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
    public void everyBreedEveryCommandDefaultBatch(GameTestHelper helper) {
        runMatrix(helper);
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 200)
    public void everyBreedEveryCommandJakeServerBatch(GameTestHelper helper) {
        runMatrix(helper);
    }

    private void runMatrix(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 1, z, Blocks.STONE);
            }
        }
        // WANDER can otherwise walk a horse out of this test's own structure bounds and into a
        // neighboring, concurrently-running test's territory (or off the edge into the void):
        // wall the whole 8x8 floor in, and keep every horse at least one block from the wall.
        for (int i = 0; i < 8; i++) {
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(i, y, 0, Blocks.BARRIER);
                helper.setBlock(i, y, 7, Blocks.BARRIER);
                helper.setBlock(0, y, i, Blocks.BARRIER);
                helper.setBlock(7, y, i, Blocks.BARRIER);
            }
        }

        BhLogWatch watch = BhLogWatch.start();
        List<AbstractHorse> horses = new ArrayList<>();
        List<HorseBreed> breeds = new ArrayList<>();
        for (HorseBreed breed : HorseBreed.values()) {
            if (breed.isRealBreed()) breeds.add(breed);
        }

        int i = 0;
        for (HorseBreed breed : breeds) {
            int x = 1 + i % 6;
            int z = 1 + i / 6;
            i++;
            AbstractHorse horse = helper.spawn(ModEntities.forBreed(breed), x, 2, z);
            IHorseData data = IHorseData.of(horse);
            data.bh_setBreed(breed);
            data.bh_setOwner(UUID.randomUUID());
            data.bh_setBond(100);
            horse.setTamed(true);
            horses.add(horse);
        }

        cycleCommands(helper, horses, 0, watch);
    }

    private static void cycleCommands(GameTestHelper helper, List<AbstractHorse> horses, int step, BhLogWatch watch) {
        HorseCommand[] commands = HorseCommand.values();
        if (step >= commands.length) {
            finish(helper, horses, watch);
            return;
        }
        HorseCommand command = commands[step];
        for (AbstractHorse horse : horses) {
            if (horse.isRemoved()) continue;
            IHorseData data = IHorseData.of(horse);
            if (command == HorseCommand.WANDER) data.bh_setWanderCenter(horse.blockPosition());
            if (command == HorseCommand.SET_HOME) data.bh_setHome(horse.blockPosition());
            data.bh_setCommand(command);
        }
        helper.runAfterDelay(TICKS_PER_COMMAND, () -> cycleCommands(helper, horses, step + 1, watch));
    }

    private static void finish(GameTestHelper helper, List<AbstractHorse> horses, BhLogWatch watch) {
        try {
            for (AbstractHorse horse : horses) {
                helper.assertTrue(!horse.isRemoved(), "a horse cycling through commands should not have been removed: "
                        + horse.getType());
                helper.assertTrue(Double.isFinite(horse.getX()) && Double.isFinite(horse.getY())
                                && Double.isFinite(horse.getZ()),
                        "a horse's position should stay finite: " + horse.getType() + " at " + horse.position());
            }

            Set<UUID> seen = new HashSet<>();
            AABB bounds = new AABB(helper.absoluteVec(new net.minecraft.world.phys.Vec3(-2, -2, -2)),
                    helper.absoluteVec(new net.minecraft.world.phys.Vec3(10, 10, 10)));
            for (AbstractHorse horse : helper.getLevel().getEntitiesOfClass(AbstractHorse.class, bounds)) {
                helper.assertTrue(seen.add(horse.getUUID()), "duplicate horse UUID found in this test's own structure: "
                        + horse.getUUID());
            }

            watch.assertClean(helper, "breed x command smoke");
        } finally {
            watch.close();
        }
        helper.succeed();
    }
}
