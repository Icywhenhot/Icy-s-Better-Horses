package icy.betterhorses.net.gametest;

import icy.betterhorses.net.BhRiderSeat;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class RiderSeatingGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void ridersOffsetApartFlatAndWhileRearing(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setTamed(true);

        Player rider1 = helper.makeMockPlayer();
        Player rider2 = helper.makeMockPlayer();
        rider1.startRiding(horse, true);
        rider2.startRiding(horse, true);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(horse.getPassengers().size() == 2, "expected two riders to have mounted");
            double flatSeparation = rider1.position().distanceTo(rider2.position());
            helper.assertTrue(flatSeparation > 0.3D,
                    "expected riders offset apart while flat (finding 8) - flatSeparation=" + flatSeparation);
            double seatY = horse.getY() + horse.getPassengersRidingOffset() + BhRiderSeat.seatLift(horse)
                    - BhRiderSeat.seatDrop(rider1);
            helper.assertTrue(Math.abs(rider1.getY() - seatY) < 1.0E-6,
                    "expected rider at seat height " + seatY + " but was " + rider1.getY());

            horse.standIfPossible();
            helper.runAfterDelay(15, () -> {
                double rearingSeparation = rider1.position().distanceTo(rider2.position());
                helper.assertTrue(rearingSeparation > 0.3D && rearingSeparation < 3.0D,
                        "expected riders still sensibly offset apart while rearing - rearingSeparation="
                                + rearingSeparation);
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 100)
    public void cartBenchRidersOffsetApartWhileFlat(GameTestHelper helper) {
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        horse.setTamed(true);
        IHorseData.of(horse).bh_getGearContainer()
                .setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_CART));

        Player rider1 = helper.makeMockPlayer();
        Player rider2 = helper.makeMockPlayer();
        rider1.startRiding(horse, true);
        rider2.startRiding(horse, true);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(horse.getPassengers().size() == 2, "expected two riders to have mounted");
            helper.assertTrue(IHorseData.of(horse).bh_hasCartGear(), "expected cart gear flag to be set");
            double flatSeparation = rider1.position().distanceTo(rider2.position());
            helper.assertTrue(flatSeparation > 0.3D,
                    "expected cart-bench riders offset apart while flat - flatSeparation=" + flatSeparation);
            helper.succeed();
        });
    }
}
