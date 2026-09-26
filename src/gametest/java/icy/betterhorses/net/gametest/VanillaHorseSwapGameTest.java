package icy.betterhorses.net.gametest;

import icy.betterhorses.net.BhVanillaHorseSwap;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class VanillaHorseSwapGameTest implements FabricGameTest {

    // Off: an already-tamed vanilla horse keeps its stats and is never swapped for a mod breed.
    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 60)
    public void tamedVanillaHorseStaysVanillaWhenConversionOff(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        horse.setTamed(true);

        helper.runAfterDelay(40, () -> {
            helper.assertTrue(horse.getClass() == Horse.class && !horse.isRemoved(),
                    "a tamed vanilla horse should stay vanilla while convert_tamed_horses is off");
            helper.succeed();
        });
    }

    // Off: an untamed vanilla horse is still converted - only taming is protected, not species.
    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 60)
    public void untamedVanillaHorseStillConvertsWhenConversionOff(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        AABB nearby = horse.getBoundingBox().inflate(4);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(horse.isRemoved(), "an untamed vanilla horse should have been swapped");
            boolean swapped = !helper.getLevel().getEntitiesOfClass(BhBreedHorse.class, nearby).isEmpty();
            helper.assertTrue(swapped, "expected a mod breed horse in its place");
            helper.succeed();
        });
    }

    // F7 (7ff72a8): a foal of two tamed vanilla horses, bred while conversion is off, must stay
    // vanilla too, tagged so BhVanillaHorseSwap skips it even though the foal itself is untamed.
    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 60)
    public void foalOfVanillaParentsStaysVanillaWhenConversionOff(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Horse parent1 = helper.spawn(EntityType.HORSE, 2, 2, 2);
        Horse parent2 = helper.spawn(EntityType.HORSE, 3, 2, 2);
        parent1.setTamed(true);
        parent2.setTamed(true);
        AABB nearby = parent1.getBoundingBox().inflate(4);

        parent1.spawnChildFromBreeding(level, parent2);

        List<Horse> foals = level.getEntitiesOfClass(Horse.class, nearby, h -> h != parent1 && h != parent2);
        helper.assertTrue(foals.size() == 1, "expected exactly one foal");
        Horse foal = foals.get(0);
        helper.assertTrue(foal.getTags().contains(BhVanillaHorseSwap.KEEP_VANILLA_TAG),
                "foal of two vanilla horses should be tagged to stay vanilla");

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(foal.getClass() == Horse.class && !foal.isRemoved(),
                    "tagged foal should still be vanilla after ticking");
            helper.succeed();
        });
    }

    // bh_disown() tags a vanilla horse to keep it vanilla, so it isn't swapped once it goes untamed.
    @GameTest(template = EMPTY_STRUCTURE, batch = BhTestBatches.JAKE_SERVER, timeoutTicks = 60)
    public void disownedVanillaHorseStaysVanillaWhenConversionOff(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        horse.setTamed(true);
        horse.setOwnerUUID(UUID.randomUUID());

        IHorseData.of(horse).bh_disown();
        helper.assertTrue(horse.getTags().contains(BhVanillaHorseSwap.KEEP_VANILLA_TAG),
                "disowning a vanilla horse while conversion is off should tag it to stay vanilla");

        helper.runAfterDelay(20, () -> {
            helper.assertTrue(horse.getClass() == Horse.class && !horse.isRemoved(),
                    "disowned+tagged vanilla horse should stay vanilla after ticking");
            helper.succeed();
        });
    }

    // On (default batch): a tamed vanilla horse IS converted, confirming the toggle actually gates it.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 60)
    public void tamedVanillaHorseConvertsByDefault(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        horse.setTamed(true);
        AABB nearby = horse.getBoundingBox().inflate(4);

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(horse.isRemoved(), "a tamed vanilla horse should convert when convert_tamed_horses is on");
            boolean swapped = !helper.getLevel().getEntitiesOfClass(BhBreedHorse.class, nearby).isEmpty();
            helper.assertTrue(swapped, "expected a mod breed horse in its place");
            helper.succeed();
        });
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 1, z, Blocks.STONE);
            }
        }
    }
}
