package icy.betterhorses.net.gametest;

import icy.betterhorses.net.BreedArchetype;
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

// Round 2, item 5: an untamed vanilla horse with custom name, leash, age, saddle/armor and health
// should convert to a mod breed cleanly - one entity, name/leash/age kept, items not lost or
// duplicated, stats within the mod's clamp rules. Default batch: convert_tamed_horses on.
public class VanillaSwapFidelityGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void swapPreservesNameLeashAgeSaddleArmorAndClampsHealth(GameTestHelper helper) {
        floor(helper);
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        // A breed set up front (isRealBreed()) keeps this test's clamp math deterministic - it
        // sidesteps the biome-pick fallback, which has its own bug covered separately below
        // (swapOfUnassignedBreedHorseKeepsThePickedBreed_KNOWN_BUG).
        IHorseData.of(horse).bh_setBreed(icy.betterhorses.net.HorseBreed.CLYDESDALE);
        horse.setCustomName(Component.literal("Silver"));
        horse.setAge(-20000); // baby
        horse.equipSaddle(null);
        horse.equipArmor(helper.makeMockServerPlayerInLevel(), new ItemStack(Items.IRON_HORSE_ARMOR));
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0D); // wildly over any breed's clamp
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

            BreedArchetype archetype = IHorseData.of(result).bh_getBreed().archetype();
            double clampedMax = archetype.clampHealth(500.0D);
            helper.assertTrue(result.getMaxHealth() <= clampedMax + 1.0e-6,
                    "max health should be clamped to the breed's archetype rules, was " + result.getMaxHealth());
            helper.assertTrue(result.getHealth() <= result.getMaxHealth(),
                    "current health should never exceed the (possibly lowered) max health");
            helper.succeed();
        });
    }

    // Confirms swapping doesn't create a second entity anywhere nearby (no stale copy left behind
    // alongside the new one) and that the old entity id is genuinely gone from the level.
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

    // Confirms the UUID really does survive: BhVanillaHorseSwap.trySwap builds `swap` via
    // horse.saveWithoutId(...) (that name only means "without the entity-TYPE id tag" - the
    // "UUID" NBT key is still written) followed by swap.load(tag), and vanilla Entity#load()
    // restores "UUID" from the tag over the entity's freshly-generated one. Worth pinning down
    // explicitly since it's easy to misread "saveWithoutId" as "without UUID".
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

    // A horse that hasn't been assigned a real breed yet (bh_getBreed() == UNKNOWN_SPECIES - the
    // ModAttachments.BhHorseSyncState default, true of any horse whose finalizeSpawn hook never
    // ran) makes BhVanillaHorseSwap.trySwap pick a fresh breed via pickForBiome/random fallback.
    // But that pick never actually lands: trySwap does `tag.putString("BH_BreedId", breed.id())`
    // on the save tag, while AbstractHorseMixin.bh_onRead only ever reads the *int* key
    // "BH_Breed" - which is still sitting in `tag` from the original (unreal) horse's own
    // bh_onWrite, since nothing overwrites it. So swap.load(tag) puts the freshly-picked breed's
    // ENTITY TYPE under the hood (ModEntities.forBreed(breed) chose the class) but then silently
    // reverts bh_getBreed() itself back to UNKNOWN_SPECIES/NONE archetype - breaking the health/
    // speed/jump clamp, chest-row count, cart eligibility, and anything else keyed off archetype.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40, required = false)
    public void swapOfUnassignedBreedHorseKeepsThePickedBreed_KNOWN_BUG(GameTestHelper helper) {
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
                    "KNOWN BUG: BhVanillaHorseSwap picks a real breed for the entity TYPE but bh_onRead reverts "
                            + "bh_getBreed() back to the original's stale BH_Breed tag - got " + resultBreed
                            + " (BhVanillaHorseSwap.trySwap writes \"BH_BreedId\" (string, never read) instead of "
                            + "overwriting \"BH_Breed\" (int, what bh_onRead actually reads))");
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
