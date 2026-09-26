package icy.betterhorses.net.gametest;

import icy.betterhorses.net.IHorseData;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;

public class SmokeGameTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void horseSpawnsWithModData(GameTestHelper helper) {
        Horse horse = helper.spawn(EntityType.HORSE, 2, 2, 2);
        helper.assertTrue(horse instanceof IHorseData, "horse should carry the mod's data");
        helper.succeed();
    }
}
