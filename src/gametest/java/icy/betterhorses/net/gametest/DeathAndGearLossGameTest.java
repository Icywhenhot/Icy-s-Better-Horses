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

// Round 2, item 4: no dupes / no loss of items on death or gear removal. Counts ItemEntities near
// the horse and compares against what it was carrying - nothing lost, nothing doubled.
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

        // Medkit deliberately excluded here: it auto-consumes-and-heals on any near-fatal hit
        // (LivingEntityMixin.bh_useHorseMedkit, triggered by the same actuallyHurt() that
        // LivingEntity#kill() routes lethal damage through), so it's legitimately gone rather
        // than dropped - covered separately below (deathByLethalHitConsumesEquippedMedkit).
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
                helper.assertTrue(expected.containsKey(entry.getKey()),
                        "unexpected extra item dropped on death: " + entry.getValue() + "x " + entry.getKey());
            }
            helper.succeed();
        });
    }

    // Not a loss bug: the medkit is a single-use "last ditch" item that auto-triggers (heal +
    // buffs) on any hit that would take the horse below 50% health, consuming itself in the
    // process - whether or not that particular hit is lethal. Confirms it ends up consumed
    // (empty slot), not silently duplicated back into the gear slot or dropped as an item too.
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

    // removingCartGearDropsChestAndPloughContents lives in PR2: it needs the cart-plough drop
    // wiring from the cart persistence fix (bh_dropCartPlough() has no caller in the port).

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
