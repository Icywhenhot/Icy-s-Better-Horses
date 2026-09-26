package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class HorseTrackerGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 60)
    public void staleCopyDiscardedPendingDisownAppliesToLiveCopy(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        UUID horseId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Vec3 pos = helper.absoluteVec(new Vec3(2.5, 2, 2.5));
        HorseTracker.setGeneration(horseId, 5);
        HorseTracker.markPendingDisown(horseId);

        Horse stale = EntityType.HORSE.create(level);
        helper.assertTrue(stale != null, "failed to create the stale horse copy");
        stale.setPos(pos);
        stale.setUUID(horseId);
        stale.setTamed(true);
        IHorseData.of(stale).bh_setOwner(ownerId);
        level.addFreshEntity(stale);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(stale.isRemoved(), "the stale copy should have been discarded on load");

            Horse live = EntityType.HORSE.create(level);
            helper.assertTrue(live != null, "failed to create the live horse copy");
            live.setPos(pos);
            live.setUUID(horseId);
            live.setTamed(true);
            IHorseData.of(live).bh_setOwner(ownerId);
            IHorseData.of(live).bh_setGeneration(5);
            level.addFreshEntity(live);

            helper.runAfterDelay(10, () -> {
                helper.assertFalse(live.isTamed(),
                        "the pending disown should have applied once the non-stale copy joined");
                helper.assertTrue(IHorseData.of(live).bh_getOwner() == null,
                        "a disowned horse should have no owner");
                helper.succeed();
            });
        });
    }
}
