package icy.betterhorses.net.gametest;

import icy.betterhorses.net.IcysBetterHorses;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class RecipeUnlockGameTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void joiningUnlocksEveryModRecipeAndNothingElse(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        List<ResourceLocation> ours = helper.getLevel().getServer().getRecipeManager().getRecipes().stream()
                .map(Recipe::getId)
                .filter(id -> IcysBetterHorses.RESOURCE_NAMESPACE.equals(id.getNamespace()))
                .toList();
        helper.assertTrue(ours.contains(new ResourceLocation(IcysBetterHorses.RESOURCE_NAMESPACE, "stable_handbook")),
                "setup: the handbook recipe should be loaded");

        IcysBetterHorses.unlockModRecipes(player);

        for (ResourceLocation id : ours) {
            helper.assertTrue(player.getRecipeBook().contains(id), id + " should be unlocked");
        }
        helper.assertFalse(player.getRecipeBook().contains(new ResourceLocation("minecraft", "diamond_sword")),
                "vanilla recipes should stay locked");
        helper.succeed();
    }
}
