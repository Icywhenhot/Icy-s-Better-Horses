package icy.betterhorses.net.gametest;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.goal.DefendOwnerGoal;
import icy.betterhorses.net.goal.HorseFollowOwnerGoal;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

// Round 2, item 5: HorseFollowOwnerGoal and DefendOwnerGoal must not keep chasing an owner/target
// that has changed dimension - a player's entity instance survives a dimension change (isAlive()
// stays true), so the goals must also check level() explicitly.
public class DimensionChangeGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void followOwnerGoalStopsWhenOwnerChangesDimension(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setCommand(HorseCommand.FOLLOW);
        horse.setTamed(true);

        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(horse.getX() + 20.0, horse.getY(), horse.getZ()); // far enough to trigger canUse()
        data.bh_setOwner(owner.getUUID());

        HorseFollowOwnerGoal goal = new HorseFollowOwnerGoal(horse);
        helper.assertTrue(goal.canUse(), "setup: goal should want to follow a far-away owner in the same level");
        helper.assertTrue(goal.canContinueToUse(), "setup: goal should still want to continue right after canUse()");

        ServerLevel nether = horse.level().getServer().getLevel(Level.NETHER);
        owner.teleportTo(nether, 0.5, 4.0, 0.5, 0.0F, 0.0F);

        helper.assertFalse(goal.canContinueToUse(),
                "the follow goal should stop once the owner is in a different dimension, however far the raw coordinates look");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void defendOwnerGoalStopsWhenTargetChangesDimension(GameTestHelper helper) {
        // Player targets only count once horse PVP is on - restore the default afterwards.
        BhConfig.apply(java.util.Map.of(BhFeature.HORSE_PVP, true), BhConfig.tuning());
        try {
            helper.setBlock(2, 1, 2, Blocks.STONE);
            AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
            IHorseData data = IHorseData.of(horse);
            data.bh_setBreed(HorseBreed.CLYDESDALE);
            horse.setTamed(true);

            ServerPlayer attacker = helper.makeMockServerPlayerInLevel();
            attacker.setPos(horse.getX(), horse.getY(), horse.getZ());
            data.bh_setCombatTarget(attacker.getUUID());

            DefendOwnerGoal goal = new DefendOwnerGoal(horse);
            helper.assertTrue(goal.canUse(), "setup: goal should want to defend against a nearby target");
            helper.assertTrue(goal.canContinueToUse(), "setup: goal should still want to continue right after canUse()");

            ServerLevel nether = horse.level().getServer().getLevel(Level.NETHER);
            Vec3 pos = horse.position();
            // Land at the horse's own raw coordinates in another dimension - distance alone would
            // look like "still right here", which is exactly why the level check has to be explicit.
            attacker.teleportTo(nether, pos.x, pos.y, pos.z, 0.0F, 0.0F);

            helper.assertFalse(goal.canContinueToUse(),
                    "the defend goal should stop once the target is in a different dimension, even at the same raw coordinates");
            helper.assertTrue(data.bh_getCombatTarget() == null, "the combat target should be cleared too");
            helper.succeed();
        } finally {
            BhConfig.apply(java.util.Map.of(BhFeature.HORSE_PVP, false), BhConfig.tuning());
        }
    }

    // Round 2, item 9: a drawn cart left behind when its horse changes dimension (no portal for
    // carts) used to sit there forever - RemovalReason.CHANGED_DIMENSION.shouldDestroy() is false,
    // so the old "only discard on shouldDestroy" check never fired for it.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void cartIsRemovedWhenItsHorseChangesDimension(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true);
        data.bh_getGearContainer().setItem(icy.betterhorses.net.inventory.GearSlot.STABILIZER.ordinal(),
                new net.minecraft.world.item.ItemStack(icy.betterhorses.net.ModItems.HORSE_CART));

        helper.runAfterDelay(5, () -> {
            icy.betterhorses.net.entity.HorseCartEntity cart = data.bh_getCartEntity();
            helper.assertTrue(cart != null && cart.isAlive(), "setup: the cart should have spawned and be alive");

            // A real portal round trip needs a working destination portal, which this test level
            // doesn't have - go straight to the post-condition the cart's tick() actually checks:
            // its horse gone from this level with RemovalReason.CHANGED_DIMENSION.
            horse.remove(net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION);

            helper.runAfterDelay(5, () -> {
                helper.assertTrue(cart.isRemoved(),
                        "the orphaned cart should be discarded once its horse has changed dimension");
                helper.succeed();
            });
        });
    }
}
