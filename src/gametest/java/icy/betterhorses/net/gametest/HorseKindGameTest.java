package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.BhContent;
import icy.betterhorses.net.BhHorseKind;
import icy.betterhorses.net.HorseTracker;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class HorseKindGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void llamasAreNotManagedButVanillaHorsesAre(GameTestHelper helper) {
        helper.assertFalse(BhHorseKind.managed(EntityType.LLAMA), "llama should not be a managed horse kind");
        helper.assertFalse(BhHorseKind.managed(EntityType.TRADER_LLAMA), "trader llama should not be a managed horse kind");
        helper.assertTrue(BhHorseKind.managed(EntityType.HORSE), "vanilla horse should be a managed horse kind");
        helper.assertTrue(BhHorseKind.managed(ModEntities.CLYDESDALE_HORSE), "breed horse should be a managed horse kind");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void tamingALlamaDoesNotClaimItAsOwned(GameTestHelper helper) {
        Llama llama = helper.spawn(EntityType.LLAMA, 2, 2, 2);
        ServerPlayer player = BhTestPlayers.at(helper, new Vec3(2, 2, 3));

        llama.tameWithName(player);

        helper.assertTrue(IHorseData.of(llama).bh_getOwner() == null,
                "taming a llama should not claim it the way taming a horse does");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void trackerRegisterIgnoresALlamaEvenIfSomehowOwned(GameTestHelper helper) {
        Llama llama = helper.spawn(EntityType.LLAMA, 2, 2, 2);
        IHorseData data = IHorseData.of(llama);
        data.bh_setOwner(UUID.randomUUID());
        llama.setTamed(true);

        HorseTracker.register(llama);

        helper.assertTrue(HorseTracker.getLoaded(llama.getUUID()) == null,
                "HorseTracker should never register a llama, owned or not");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void radialCommandIgnoresAnOwnedTamedLlama(GameTestHelper helper) {
        Llama llama = helper.spawn(EntityType.LLAMA, 2, 2, 2);
        UUID ownerId = UUID.randomUUID();
        IHorseData data = IHorseData.of(llama);
        data.bh_setOwner(ownerId);
        llama.setTamed(true);
        ServerPlayer owner = BhTestPlayers.owner(helper, new Vec3(2, 2, 3), ownerId);

        IcysBetterHorses.handleRadialCommand(owner, llama.getId(), BhContent.COMMAND_WANDER.key());

        helper.assertTrue(data.bh_getCommand() != BhContent.COMMAND_WANDER.key(),
                "the whistle/radial command path should never target a llama");
        helper.succeed();
    }
}
