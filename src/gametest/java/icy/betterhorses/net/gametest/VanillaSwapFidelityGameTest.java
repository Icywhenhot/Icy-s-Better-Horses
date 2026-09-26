package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.ArchetypeType;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.entity.BhBreedHorse;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class VanillaSwapFidelityGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void swapPreservesNameLeashAgeSaddleArmorAndClampsHealth(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        IHorseData.of(horse).bh_setBreed(icy.betterhorses.net.HorseBreed.CLYDESDALE);
        horse.setCustomName(Component.literal("Silver"));
        horse.setAge(-20000);
        horse.equipSaddle(null);
        horse.equipArmor(helper.makeMockServerPlayerInLevel(), new ItemStack(Items.IRON_HORSE_ARMOR));
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0D);
        horse.setHealth(horse.getMaxHealth());

        Pig anchor = helper.spawn(EntityType.PIG, 3, 2, 3);
        horse.setLeashedTo(anchor, true);
        helper.assertTrue(horse.isLeashed(), "setup: horse should be leashed before conversion");

        AABB nearby = horse.getBoundingBox().inflate(4);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(horse.isRemoved(), "the vanilla horse should have been swapped");
            List<BhBreedHorse> swapped = helper.getLevel().getEntitiesOfClass(BhBreedHorse.class, nearby);
            helper.assertTrue(swapped.size() == 1, "expected exactly one mod breed horse after the swap, found "
                    + swapped.size());
            BhBreedHorse result = swapped.get(0);

            helper.assertTrue(result.hasCustomName() && "Silver".equals(result.getCustomName().getString()),
                    "custom name should survive the swap");
            helper.assertTrue(result.isBaby(), "baby/age should survive the swap");
            helper.assertTrue(result.isLeashed() && result.getLeashHolder() == anchor,
                    "the leash should survive the swap, attached to the same anchor");
            helper.assertTrue(result.isSaddled(), "the saddle should survive the swap");
            helper.assertTrue(result.getArmor().is(Items.IRON_HORSE_ARMOR), "the armor should survive the swap");

            ArchetypeType archetype = IHorseData.of(result).bh_getBreed().archetype();
            double clampedMax = archetype.highHealth();
            helper.assertTrue(result.getMaxHealth() <= clampedMax + 1.0e-6,
                    "max health should be clamped to the breed's archetype rules, was " + result.getMaxHealth());
            helper.assertTrue(result.getHealth() <= result.getMaxHealth(),
                    "current health should never exceed the (possibly lowered) max health");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void swapLeavesExactlyOneEntityBehind(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        int oldId = horse.getId();
        AABB nearby = horse.getBoundingBox().inflate(4);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(helper.getLevel().getEntity(oldId) == null,
                    "the old vanilla horse's entity id should no longer resolve to anything");
            List<Horse> allHorses = helper.getLevel().getEntitiesOfClass(Horse.class, nearby);
            helper.assertTrue(allHorses.size() == 1, "expected exactly one horse entity after the swap, found "
                    + allHorses.size());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void swapKeepsSameUUID(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        java.util.UUID before = horse.getUUID();
        AABB nearby = horse.getBoundingBox().inflate(4);

        helper.runAfterDelay(10, () -> {
            List<BhBreedHorse> swapped = helper.getLevel().getEntitiesOfClass(BhBreedHorse.class, nearby);
            helper.assertTrue(swapped.size() == 1, "setup: expected exactly one mod breed horse after the swap");
            helper.assertTrue(swapped.get(0).getUUID().equals(before),
                    "the horse's UUID should survive vanilla -> mod breed conversion - was " + before
                            + ", now " + swapped.get(0).getUUID());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void swapOfUnassignedBreedHorseKeepsThePickedBreed(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        helper.assertFalse(IHorseData.of(horse).bh_getBreed().isRealBreed(),
                "setup: a freshly test-spawned horse should not have a real breed assigned yet");
        AABB nearby = horse.getBoundingBox().inflate(4);

        helper.runAfterDelay(10, () -> {
            List<BhBreedHorse> swapped = helper.getLevel().getEntitiesOfClass(BhBreedHorse.class, nearby);
            helper.assertTrue(swapped.size() == 1, "setup: expected exactly one mod breed horse after the swap");
            HorseBreed resultBreed = IHorseData.of(swapped.get(0)).bh_getBreed();
            helper.assertTrue(resultBreed.isRealBreed(),
                    "expected the freshly-picked breed to stick, got " + resultBreed);
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
