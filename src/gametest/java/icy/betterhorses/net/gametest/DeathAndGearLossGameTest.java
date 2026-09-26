package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathAndGearLossGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void deathDropsSaddleArmorGearAndChestContentsExactlyOnce(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setOwner(UUID.randomUUID());
        horse.setTamed(true);
        horse.equipSaddle(null);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        horse.equipArmor(player, new ItemStack(Items.IRON_HORSE_ARMOR));

        data.bh_getGearContainer().setItem(GearSlot.HOOVES.ordinal(), new ItemStack(ModItems.HORSE_HOOVES));
        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_STABILIZER));
        data.bh_getGearContainer().setItem(GearSlot.CHEST.ordinal(), new ItemStack(Items.CHEST));
        data.bh_getChestContainer().setItem(0, new ItemStack(Items.DIAMOND, 3));
        data.bh_getChestContainer().setItem(5, new ItemStack(Items.GOLD_INGOT, 5));

        Map<Item, Integer> expected = new HashMap<>();
        expected.put(Items.SADDLE, 1);
        expected.put(Items.IRON_HORSE_ARMOR, 1);
        expected.put(ModItems.HORSE_HOOVES, 1);
        expected.put(ModItems.HORSE_STABILIZER, 1);
        expected.put(Items.CHEST, 1);
        expected.put(Items.DIAMOND, 3);
        expected.put(Items.GOLD_INGOT, 5);

        horse.kill();

        helper.runAfterDelay(5, () -> {
            Map<Item, Integer> found = countDrops(helper, horse);
            for (Map.Entry<Item, Integer> entry : expected.entrySet()) {
                int count = found.getOrDefault(entry.getKey(), 0);
                helper.assertTrue(count == entry.getValue(),
                        "expected exactly " + entry.getValue() + "x " + entry.getKey()
                                + " dropped on death, found " + count);
            }
            for (Map.Entry<Item, Integer> entry : found.entrySet()) {
                if (entry.getKey() == Items.LEATHER) continue;
                helper.assertTrue(expected.containsKey(entry.getKey()),
                        "unexpected extra item dropped on death: " + entry.getValue() + "x " + entry.getKey());
            }
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void deathByLethalHitConsumesEquippedMedkit(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_getGearContainer().setItem(GearSlot.MEDKIT.ordinal(), new ItemStack(ModItems.HORSE_MEDKIT));

        horse.kill();

        helper.runAfterDelay(5, () -> {
            Map<Item, Integer> found = countDrops(helper, horse);
            helper.assertTrue(found.getOrDefault(ModItems.HORSE_MEDKIT, 0) == 0,
                    "medkit should be consumed (not dropped) when it auto-triggers on a lethal hit, found "
                            + found.getOrDefault(ModItems.HORSE_MEDKIT, 0));
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void removingCartGearDropsChestAndPloughContents(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_CART));
        helper.assertTrue(data.bh_hasCartGear(), "setup: cart gear should be equipped");
        data.bh_setCartChest(new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(0, new ItemStack(Items.IRON_INGOT, 4));
        data.bh_setCartPlough(new ItemStack(Items.IRON_HOE));

        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), ItemStack.EMPTY);
        helper.assertFalse(data.bh_hasCartGear(), "setup: cart gear should now be unequipped");

        helper.runAfterDelay(5, () -> {
            helper.assertFalse(data.bh_hasCartChest(), "cart chest flag should clear once cart gear is removed");
            helper.assertTrue(data.bh_getCartChestContainer().isEmpty(), "cart chest container should be emptied");
            helper.assertFalse(data.bh_hasCartPlough(), "cart plough flag should clear once cart gear is removed");

            Map<Item, Integer> found = countDrops(helper, horse);
            helper.assertTrue(found.getOrDefault(Items.IRON_INGOT, 0) == 4,
                    "expected the cart chest's 4 iron ingots to drop, found "
                            + found.getOrDefault(Items.IRON_INGOT, 0));
            helper.assertTrue(found.getOrDefault(Items.IRON_HOE, 0) == 1,
                    "expected the cart plough to drop, found " + found.getOrDefault(Items.IRON_HOE, 0));
            helper.assertTrue(found.getOrDefault(ModItems.HORSE_CART, 0) == 0,
                    "the cart item itself was emptied directly (not dropped), should not also appear on the ground");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void removingChestGearWithItemsInsideDropsContents(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_getGearContainer().setItem(GearSlot.CHEST.ordinal(), new ItemStack(Items.CHEST));
        data.bh_getChestContainer().setItem(0, new ItemStack(Items.EMERALD, 2));
        data.bh_getChestContainer().setItem(1, new ItemStack(Items.NETHERITE_INGOT, 1));

        ItemStack previous = data.bh_getGearContainer().getItem(GearSlot.CHEST.ordinal());
        data.bh_getGearContainer().setItem(GearSlot.CHEST.ordinal(), ItemStack.EMPTY);
        data.bh_onChestGearRemoved(previous);

        helper.assertTrue(data.bh_getChestContainer().isEmpty(), "chest container should be emptied when chest gear is removed");
        Map<Item, Integer> found = countDrops(helper, horse);
        helper.assertTrue(found.getOrDefault(Items.EMERALD, 0) == 2,
                "expected 2 emeralds to drop when chest gear was removed, found " + found.getOrDefault(Items.EMERALD, 0));
        helper.assertTrue(found.getOrDefault(Items.NETHERITE_INGOT, 0) == 1,
                "expected the netherite ingot to drop when chest gear was removed, found "
                        + found.getOrDefault(Items.NETHERITE_INGOT, 0));
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 40)
    public void deathWithCartDropsChestAndPloughContents(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setOwner(UUID.randomUUID());
        horse.setTamed(true);
        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_CART));
        data.bh_setCartChest(new ItemStack(Items.CHEST));
        data.bh_getCartChestContainer().setItem(0, new ItemStack(Items.DIAMOND, 7));
        data.bh_setCartPlough(new ItemStack(Items.IRON_HOE));

        horse.kill();

        Map<Item, Integer> found = countDrops(helper, horse);
        helper.assertTrue(found.getOrDefault(Items.DIAMOND, 0) == 7,
                "expected the cart chest's 7 diamonds to drop on death, found "
                        + found.getOrDefault(Items.DIAMOND, 0));
        helper.assertTrue(found.getOrDefault(Items.IRON_HOE, 0) == 1,
                "expected the cart plough to drop on death, found "
                        + found.getOrDefault(Items.IRON_HOE, 0));
        helper.succeed();
    }

    private static Map<Item, Integer> countDrops(GameTestHelper helper, AbstractHorse horse) {
        AABB area = new AABB(horse.getX() - 6, horse.getY() - 6, horse.getZ() - 6,
                horse.getX() + 6, horse.getY() + 6, horse.getZ() + 6);
        List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area);
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return counts;
    }
}
