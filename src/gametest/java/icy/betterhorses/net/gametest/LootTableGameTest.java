package icy.betterhorses.net.gametest;

import icy.betterhorses.net.registry.BhBreeds;
import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;

public class LootTableGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void everyBreedHasANonEmptyDefaultLootTable(GameTestHelper helper) {
        LootDataManager lootData = helper.getLevel().getServer().getLootData();
        for (HorseBreed breed : HorseBreed.values()) {
            if (!breed.isRealBreed()) continue;
            EntityType<?> type = ModEntities.forBreed(BhBreeds.keyOf(breed));
            LootTable table = lootData.getLootTable(type.getDefaultLootTable());
            helper.assertTrue(table != LootTable.EMPTY,
                    "expected " + breed + " (" + type.getDefaultLootTable() + ") to have a loot table, got the empty one");
        }
        helper.succeed();
    }
}
