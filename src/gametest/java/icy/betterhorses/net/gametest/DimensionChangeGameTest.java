package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.BhFeature;
import icy.betterhorses.net.HorseBreed;
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

public class DimensionChangeGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void followOwnerGoalStopsWhenOwnerChangesDimension(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setCommand(BhContent.COMMAND_FOLLOW.key());
        horse.setTamed(true);

        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(horse.getX() + 20.0, horse.getY(), horse.getZ());
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
            attacker.teleportTo(nether, pos.x, pos.y, pos.z, 0.0F, 0.0F);

            helper.assertFalse(goal.canContinueToUse(),
                    "the defend goal should stop once the target is in a different dimension, even at the same raw coordinates");
            helper.assertTrue(data.bh_getCombatTarget() == null, "the combat target should be cleared too");
            helper.succeed();
        } finally {
            BhConfig.apply(java.util.Map.of(BhFeature.HORSE_PVP, false), BhConfig.tuning());
        }
    }

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

            horse.remove(net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION);

            helper.runAfterDelay(5, () -> {
                helper.assertTrue(cart.isRemoved(),
                        "the orphaned cart should be discarded once its horse has changed dimension");
                helper.succeed();
            });
        });
    }
}
