package icy.betterhorses.net.gametest;

import com.mojang.authlib.GameProfile;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.registry.BhContent;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// A horse nobody is riding gets itself off a block that is hurting it, whatever it was told to do.
// It only ever walks: one with no way out stays put and its owner is told. Each test has its own
// raised floor, reaching past the test structure, and the horse starts at 2, 4 on it.
public class HazardEscapeGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 140)
    public void aHorseToldToStayStepsOffACampfire(GameTestHelper helper) {
        int y = 200;
        AbstractHorse horse = horseOnACampfire(helper, y);
        ServerPlayer owner = owner(helper, horse, 10.5D, y + 1, 4.5D, BhContent.COMMAND_STAY.key());
        AABB campfire = campfire(helper, y);

        helper.runAfterDelay(100, () -> {
            boolean alive = horse.isAlive();
            boolean onIt = horse.getBoundingBox().intersects(campfire);
            cleanUp(horse, owner);
            helper.assertTrue(alive, "the horse should have lived");
            helper.assertTrue(!onIt, "a horse told to stay should still step off a campfire that is hurting it");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 80)
    public void aWanderingHorseStepsOffACampfireAndKeepsItsWanderCentre(GameTestHelper helper) {
        int y = 205;
        AbstractHorse horse = horseOnACampfire(helper, y);
        ServerPlayer owner = owner(helper, horse, 10.5D, y + 1, 4.5D, BhContent.COMMAND_WANDER.key());
        BlockPos centre = horse.blockPosition();
        IHorseData.of(horse).bh_setWanderCenter(centre);
        AABB campfire = campfire(helper, y);

        // sooner than its idle strolling is likely to take it off by chance
        helper.runAfterDelay(40, () -> {
            boolean onIt = horse.getBoundingBox().intersects(campfire);
            BlockPos kept = IHorseData.of(horse).bh_getWanderCenter();
            cleanUp(horse, owner);
            helper.assertTrue(!onIt, "a wandering horse should step off a campfire that is hurting it");
            helper.assertTrue(centre.equals(kept), "stepping off must not move its wander centre, now " + kept);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 160)
    public void aFollowingHorseRestingOnACampfireStepsOffAndStaysOff(GameTestHelper helper) {
        int y = 210;
        AbstractHorse horse = horseOnACampfire(helper, y);
        // close enough that following alone would leave it standing where it is
        ServerPlayer owner = owner(helper, horse, 7.0D, y + 1, 4.5D, BhContent.COMMAND_FOLLOW.key());
        AABB campfire = campfire(helper, y);

        // given three seconds to get off before it counts as being on it again
        int[] ticks = {0};
        boolean[] wentBack = {false};
        helper.onEachTick(() -> {
            if (++ticks[0] > 60 && horse.isAlive() && horse.getBoundingBox().intersects(campfire)) wentBack[0] = true;
        });

        helper.runAfterDelay(120, () -> {
            cleanUp(horse, owner);
            helper.assertTrue(!wentBack[0], "a following horse should get off the campfire and not go back onto it");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 200)
    public void aHorseWithNoWayOutStaysAndItsOwnerIsToldOnce(GameTestHelper helper) {
        int y = 215;
        floor(helper, y);
        pit(helper, y);
        AbstractHorse horse = horse(helper, y);
        Listener owner = new Listener(helper.getLevel());
        owner.setPos(helper.absoluteVec(new Vec3(10.5D, y + 1, 4.5D)));
        helper.getLevel().addNewPlayer(owner);
        IHorseData data = IHorseData.of(horse);
        data.bh_setOwner(owner.getUUID());
        data.bh_setCommand(BhContent.COMMAND_STAY.key());
        // long enough to be told more than once
        horse.setHealth(horse.getMaxHealth());
        BlockPos start = horse.blockPosition();

        helper.runAfterDelay(160, () -> {
            double strayed = Math.sqrt(horse.blockPosition().distSqr(start));
            String where = horse.getBlockX() + ", " + horse.getBlockY() + ", " + horse.getBlockZ();
            List<String> heard = List.copyOf(owner.heard);
            cleanUp(horse, owner);
            helper.assertTrue(strayed <= 2.0D, "nothing may lift a trapped horse out, but it moved "
                    + String.format("%.1f", strayed) + " blocks");
            helper.assertTrue(heard.size() == 1, "the owner should be told once that the horse is trapped, heard: " + heard);
            helper.assertTrue(heard.get(0).contains(where),
                    "the message should say where the horse is (" + where + "), heard: " + heard);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 80)
    public void aRiddenHorseIsLeftToItsRider(GameTestHelper helper) {
        int y = 220;
        AbstractHorse horse = horseOnACampfire(helper, y);
        ServerPlayer owner = owner(helper, horse, 2.5D, y + 2, 4.5D, BhContent.COMMAND_STAY.key());
        owner.startRiding(horse, true);
        float before = horse.getHealth();

        helper.runAfterDelay(40, () -> {
            boolean ridden = horse.isVehicle();
            boolean hurt = horse.getHealth() < before;
            int escaping = IHorseData.of(horse).bh_getHazardEscapeTicks();
            cleanUp(horse, owner);
            helper.assertTrue(ridden && hurt, "the test needs a ridden horse the campfire has hurt");
            helper.assertTrue(escaping == 0, "a ridden horse must not start getting away on its own");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 80)
    public void mountingAHorseEndsItsGetaway(GameTestHelper helper) {
        int y = 225;
        AbstractHorse horse = horseOnACampfire(helper, y);
        ServerPlayer owner = owner(helper, horse, 10.5D, y + 1, 4.5D, BhContent.COMMAND_STAY.key());

        boolean[] wasEscaping = {false};
        helper.runAfterDelay(12, () -> {
            wasEscaping[0] = IHorseData.of(horse).bh_getHazardEscapeTicks() > 0;
            owner.startRiding(horse, true);
        });

        helper.runAfterDelay(16, () -> {
            boolean stillGoing = !horse.getNavigation().isDone();
            cleanUp(horse, owner);
            helper.assertTrue(wasEscaping[0], "the horse should have been getting away before it was mounted");
            helper.assertTrue(!stillGoing, "a horse that has just been mounted must stop walking itself away");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void aHorseHurtByAMobDoesNotTreatItAsAHazard(GameTestHelper helper) {
        int y = 230;
        floor(helper, y);
        AbstractHorse horse = horse(helper, y);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 5, y + 1, 4);

        helper.runAfterDelay(2, () -> {
            boolean hurt = horse.hurt(horse.damageSources().mobAttack(zombie), 1.0F);
            int escaping = IHorseData.of(horse).bh_getHazardEscapeTicks();
            zombie.discard();
            horse.discard();
            helper.assertTrue(hurt, "the test needs the hit to land");
            helper.assertTrue(escaping == 0, "a hit from a mob is the combat code's business, not a hazard");
            helper.succeed();
        });
    }

    private static AbstractHorse horseOnACampfire(GameTestHelper helper, int y) {
        floor(helper, y);
        helper.setBlock(2, y + 1, 4, Blocks.CAMPFIRE);
        return horse(helper, y);
    }

    private static void floor(GameTestHelper helper, int y) {
        for (int x = 0; x <= 12; x++) {
            for (int z = 1; z <= 7; z++) {
                helper.setBlock(x, y, z, Blocks.STONE);
            }
        }
    }

    private static AbstractHorse horse(GameTestHelper helper, int floorY) {
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, floorY + 1, 4);
        IHorseData.of(horse).bh_setBreed(HorseBreed.CLYDESDALE);
        horse.setTamed(true);
        return horse;
    }

    private static AABB campfire(GameTestHelper helper, int y) {
        return new AABB(helper.absolutePos(new BlockPos(2, y + 1, 4)));
    }

    // a magma floor under the horse's starting spot, walled in on every side too high to jump
    private static void pit(GameTestHelper helper, int floorY) {
        for (int x = 0; x <= 4; x++) {
            for (int z = 2; z <= 6; z++) {
                if (x != 0 && x != 4 && z != 2 && z != 6) {
                    helper.setBlock(x, floorY, z, Blocks.MAGMA_BLOCK);
                    continue;
                }
                for (int up = 1; up <= 4; up++) {
                    helper.setBlock(x, floorY + up, z, Blocks.STONE);
                }
            }
        }
    }

    private static ServerPlayer owner(GameTestHelper helper, AbstractHorse horse, double x, double y, double z,
                                      net.minecraft.resources.ResourceKey<icy.betterhorses.net.registry.CommandType> command) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        owner.setPos(helper.absoluteVec(new Vec3(x, y, z)));
        IHorseData data = IHorseData.of(horse);
        data.bh_setOwner(owner.getUUID());
        data.bh_setCommand(command);
        return owner;
    }

    private static void cleanUp(AbstractHorse horse, ServerPlayer owner) {
        horse.discard();
        owner.discard();
    }

    private static final class Listener extends FakePlayer {
        // only the mod's own messages: the test server also tells every player how each test went
        final List<String> heard = new ArrayList<>();

        Listener(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "bh-listener"));
        }

        @Override
        public void sendSystemMessage(Component message, boolean overlay) {
            if (message.getContents() instanceof TranslatableContents said
                    && said.getKey().startsWith("message.icys-better-horses.")) {
                heard.add(message.getString());
            }
        }
    }
}
