package icy.betterhorses.net.gametest;

import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class HorsePersistenceGameTest implements FabricGameTest {

    // BH_Generation (6323166): a horse's whistle generation must survive a save/load round-trip,
    // or a restart discards horses the tracker still thinks are "newer" than what got saved.
    @GameTest(template = EMPTY_STRUCTURE)
    public void generationRoundTrips(GameTestHelper helper) {
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData.of(original).bh_setGeneration(7);

        AbstractHorse loaded = reload(helper, original);

        helper.assertTrue(IHorseData.of(loaded).bh_getGeneration() == 7,
                "generation should survive a save/load round-trip");
        helper.succeed();
    }

    private static AbstractHorse reload(GameTestHelper helper, AbstractHorse original) {
        ServerLevel level = helper.getLevel();
        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        AbstractHorse loaded = ModEntities.CLYDESDALE_HORSE.create(level);
        helper.assertTrue(loaded != null, "failed to create a fresh Clydesdale for load");
        loaded.load(saved);
        return loaded;
    }
}
