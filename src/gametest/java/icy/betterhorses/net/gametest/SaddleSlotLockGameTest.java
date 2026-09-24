package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.lang.reflect.Field;

// Round 2, item 8: the saddle can't be picked up while cart gear is fitted. The screen mixin only
// stops a well-behaved client from sending the click; a modified client can send it directly, so
// the menu's own saddle slot must refuse the pickup server-side too.
public class SaddleSlotLockGameTest implements FabricGameTest {

    private static final int SADDLE_SLOT = 0;

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void saddleSlotRefusesPickupServerSideWhileCartFitted(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        horse.equipSaddle(null);
        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_CART));
        helper.assertTrue(data.bh_hasCartGear(), "setup: cart gear should be equipped");

        Player player = helper.makeMockPlayer();
        HorseInventoryMenu menu = openMenuBypassingConnection(horse, player);

        ItemStack before = menu.getSlot(SADDLE_SLOT).getItem();
        helper.assertTrue(before.is(Items.SADDLE), "setup: the saddle slot should hold a saddle");

        menu.clicked(SADDLE_SLOT, 0, ClickType.PICKUP, player);

        helper.assertTrue(menu.getSlot(SADDLE_SLOT).getItem().is(Items.SADDLE),
                "the saddle should still be in the slot - the server should have refused the pickup");
        helper.assertTrue(player.getInventory().getItem(0).isEmpty() || !player.getInventory().getItem(0).is(Items.SADDLE),
                "the player should not have received the saddle from a refused pickup");
        helper.succeed();
    }

    // No network connection needed for this - go straight at the same constructor
    // openCustomInventoryScreen() would use, skipping the open-screen packet.
    private static HorseInventoryMenu openMenuBypassingConnection(AbstractHorse horse, Player player) {
        try {
            Field inventoryField = AbstractHorse.class.getDeclaredField("inventory");
            inventoryField.setAccessible(true);
            SimpleContainer horseContainer = (SimpleContainer) inventoryField.get(horse);
            return new HorseInventoryMenu(1, player.getInventory(), horseContainer, horse);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
