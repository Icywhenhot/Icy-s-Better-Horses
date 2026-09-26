package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class HorsePersistenceGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void generationRoundTrips(GameTestHelper helper) {
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData.of(original).bh_setGeneration(7);

        AbstractHorse loaded = reload(helper, original);

        helper.assertTrue(IHorseData.of(loaded).bh_getGeneration() == 7,
                "generation should survive a save/load round-trip");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void cartChestAndPloughRoundTrip(GameTestHelper helper) {
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(original);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setCartChest(new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(0, new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(53, new ItemStack(Items.DIAMOND, 4));
        data.bh_setCartPlough(new ItemStack(Items.IRON_HOE));

        AbstractHorse loaded = reload(helper, original);
        IHorseData loadedData = IHorseData.of(loaded);

        helper.assertTrue(loadedData.bh_hasCartChest(), "cart chest flag should survive save/load");
        helper.assertTrue(loadedData.bh_getCartChestContainer().getItem(0).is(Items.CHEST),
                "cart chest slot 0 contents should survive save/load");
        helper.assertTrue(loadedData.bh_getCartChestContainer().getItem(53).is(Items.DIAMOND),
                "cart chest last slot contents should survive save/load");
        helper.assertTrue(loadedData.bh_getCartPlough().is(Items.IRON_HOE), "cart plough item should survive save/load");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void largeCartFlagRoundTrip(GameTestHelper helper) {
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(original);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setLargeCart(true);
        helper.assertTrue(data.bh_hasLargeCart(), "setup: large cart flag should be settable on a draft breed");

        AbstractHorse loaded = reload(helper, original);
        helper.assertTrue(IHorseData.of(loaded).bh_hasLargeCart(), "large cart flag should survive save/load");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void legacySaveWithoutCartKeysDefaultsToNoCart(GameTestHelper helper) {
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(original);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setCartChest(new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(0, new ItemStack(Items.CHEST));
        data.bh_setCartPlough(new ItemStack(Items.IRON_HOE));
        data.bh_setLargeCart(true);

        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        saved.remove("BH_CartChestOn");
        saved.remove("BH_CartChestItem");
        saved.remove("BH_CartChest");
        saved.remove("BH_CartChestItems");
        saved.remove("BH_CartPlow");
        saved.remove("BH_CartLarge");

        AbstractHorse loaded = ModEntities.CLYDESDALE_HORSE.create(helper.getLevel());
        helper.assertTrue(loaded != null, "failed to create a fresh Clydesdale for load");
        loaded.load(saved);
        IHorseData loadedData = IHorseData.of(loaded);

        helper.assertFalse(loadedData.bh_hasCartChest(), "legacy save should load with no cart chest");
        helper.assertTrue(loadedData.bh_getCartChestContainer().isEmpty(), "legacy save should load with an empty cart chest");
        helper.assertFalse(loadedData.bh_hasCartPlough(), "legacy save should load with no plough");
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void legacyCartChestFormatLoadsAndThenResavesInTheNewFormat(GameTestHelper helper) {
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(original);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setCartChest(new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(0, new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(53, new ItemStack(Items.DIAMOND, 4));

        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        net.minecraft.nbt.Tag items = saved.get("BH_CartChest");
        boolean on = saved.getBoolean("BH_CartChestOn");
        saved.remove("BH_CartChestOn");
        saved.remove("BH_CartChest");
        saved.putBoolean("BH_CartChest", on);
        saved.put("BH_CartChestItems", items);

        ServerLevel level = helper.getLevel();
        AbstractHorse loaded = ModEntities.CLYDESDALE_HORSE.create(level);
        helper.assertTrue(loaded != null, "failed to create a fresh Clydesdale for load");
        loaded.load(saved);
        IHorseData loadedData = IHorseData.of(loaded);

        helper.assertTrue(loadedData.bh_hasCartChest(), "the old-format boolean flag should still be read");
        helper.assertTrue(loadedData.bh_getCartChestContainer().getItem(0).is(Items.CHEST),
                "the old-format BH_CartChestItems contents should still be read");
        helper.assertTrue(loadedData.bh_getCartChestContainer().getItem(53).is(Items.DIAMOND),
                "the old-format BH_CartChestItems last slot should still be read");

        CompoundTag resaved = loaded.saveWithoutId(new CompoundTag());
        helper.assertTrue(resaved.contains("BH_CartChestOn"), "re-saving should always write the new BH_CartChestOn key");
        helper.assertTrue(resaved.contains("BH_CartChest", net.minecraft.nbt.Tag.TAG_LIST),
                "re-saving should always write BH_CartChest as the new item-list format");
        helper.assertFalse(resaved.contains("BH_CartChestItems"), "re-saving should not keep writing the legacy key");
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
