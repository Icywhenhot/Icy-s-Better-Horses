package icy.betterhorses.net.gametest;

import icy.betterhorses.net.HorseBreed;
import icy.betterhorses.net.HorseCommand;
import icy.betterhorses.net.HorseGender;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import icy.betterhorses.net.inventory.GearSlot;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

// Round 2, item 3: for every breed, set every property IHorseData exposes a setter for, save,
// load into a fresh entity, and compare every getter that AbstractHorseMixin's bh_onWrite/bh_onRead
// actually persist. Also documents (separately) the fields that intentionally reset on reload, and
// one that never gets a chance to persist because nothing ever sets it in the first place.
public class HorseStateRoundTripGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void fullStateRoundTripPerBreed(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, net.minecraft.world.level.block.Blocks.STONE);
        int i = 0;
        for (HorseBreed breed : HorseBreed.values()) {
            if (!breed.isRealBreed()) continue;
            roundTripOneBreed(helper, breed, i++);
        }
        helper.succeed();
    }

    private void roundTripOneBreed(GameTestHelper helper, HorseBreed breed, int index) {
        String ctx = breed.id() + ": ";
        AbstractHorse original = helper.spawn(ModEntities.forBreed(breed), 2, 2, 2);
        IHorseData data = IHorseData.of(original);

        UUID owner = UUID.randomUUID();
        data.bh_setOwner(owner);
        HorseCommand command = HorseCommand.values()[index % HorseCommand.values().length];
        data.bh_setCommand(command);
        int bond = 10 + (index * 7) % 90;
        data.bh_setBond(bond);
        data.bh_setReceivedNameTagBond(true);
        data.bh_setBondRemainder(index % 13);
        BlockPos home = helper.absolutePos(new BlockPos(1, 2, 1));
        data.bh_setHome(home);
        BlockPos wander = helper.absolutePos(new BlockPos(3, 2, 5));
        data.bh_setWanderCenter(wander);
        HorseGender gender = index % 2 == 0 ? HorseGender.FEMALE : HorseGender.MALE;
        data.bh_setGender(gender);
        data.bh_setBreed(breed);
        data.bh_setMixedBreed(true);
        int generation = 1 + index;
        data.bh_setGeneration(generation);
        data.bh_setAbilityPaused(true);
        long rescueReadyAt = 1000L + index;
        data.bh_setRescueReadyAt(rescueReadyAt);

        data.bh_getGearContainer().setItem(GearSlot.CHEST.ordinal(), new ItemStack(Items.CHEST));
        data.bh_getGearContainer().setItem(GearSlot.HOOVES.ordinal(), new ItemStack(ModItems.HORSE_HOOVES));
        data.bh_getGearContainer().setItem(GearSlot.MEDKIT.ordinal(), new ItemStack(ModItems.HORSE_MEDKIT));
        data.bh_getGearContainer().setItem(GearSlot.STABILIZER.ordinal(), new ItemStack(ModItems.HORSE_STABILIZER));
        data.bh_getChestContainer().setItem(0, new ItemStack(Items.DIAMOND, 3));
        data.bh_getChestContainer().setItem(26, new ItemStack(Items.GOLD_INGOT, 5));
        data.bh_equipUpgradedSaddle(new ItemStack(ModItems.UPGRADED_SADDLE));

        data.bh_setCartChest(true);
        data.bh_getCartChestContainer().setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        data.bh_getCartChestContainer().setItem(52, new ItemStack(Items.EMERALD, 1));
        data.bh_setCartPlough(new ItemStack(Items.IRON_HOE));
        data.bh_setLargeCart(true);

        ServerLevel level = helper.getLevel();
        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        AbstractHorse loaded = ModEntities.forBreed(breed).create(level);
        helper.assertTrue(loaded != null, ctx + "failed to create a fresh horse for load");
        loaded.load(saved);
        IHorseData loadedData = IHorseData.of(loaded);

        helper.assertTrue(owner.equals(loadedData.bh_getOwner()), ctx + "owner should survive");
        helper.assertTrue(loadedData.bh_getCommand() == command, ctx + "command should survive");
        helper.assertTrue(loadedData.bh_getBond() == bond, ctx + "bond should survive");
        helper.assertTrue(loadedData.bh_hasReceivedNameTagBond(), ctx + "received-name-tag-bond flag should survive");
        helper.assertTrue(loadedData.bh_getBondRemainder() == data.bh_getBondRemainder(),
                ctx + "bond remainder should survive");
        helper.assertTrue(home.equals(loadedData.bh_getHome()), ctx + "home should survive");
        helper.assertTrue(data.bh_getHomeDimension() != null && data.bh_getHomeDimension().equals(loadedData.bh_getHomeDimension()),
                ctx + "home dimension should survive");
        helper.assertTrue(wander.equals(loadedData.bh_getWanderCenter()), ctx + "wander center should survive");
        helper.assertTrue(loadedData.bh_getGender() == gender, ctx + "gender should survive");
        helper.assertTrue(loadedData.bh_getBreed() == breed, ctx + "breed should survive");
        helper.assertTrue(loadedData.bh_isMixedBreed(), ctx + "mixed-breed flag should survive");
        helper.assertTrue(loadedData.bh_getGeneration() == generation, ctx + "generation should survive");
        helper.assertTrue(loadedData.bh_isAbilityPaused(), ctx + "ability-paused should survive");
        helper.assertTrue(loadedData.bh_getRescueReadyAt() == rescueReadyAt, ctx + "rescue-ready-at should survive");

        helper.assertTrue(loadedData.bh_getGearContainer().getItem(GearSlot.CHEST.ordinal()).is(Items.CHEST),
                ctx + "chest gear item should survive");
        helper.assertTrue(loadedData.bh_getGearContainer().getItem(GearSlot.HOOVES.ordinal()).is(ModItems.HORSE_HOOVES),
                ctx + "hooves gear item should survive");
        helper.assertTrue(loadedData.bh_getGearContainer().getItem(GearSlot.MEDKIT.ordinal()).is(ModItems.HORSE_MEDKIT),
                ctx + "medkit gear item should survive");
        helper.assertTrue(loadedData.bh_getGearContainer().getItem(GearSlot.STABILIZER.ordinal()).is(ModItems.HORSE_STABILIZER),
                ctx + "stabilizer gear item should survive");
        helper.assertTrue(loadedData.bh_hasGear(GearSlot.CHEST) && loadedData.bh_hasGear(GearSlot.HOOVES)
                        && loadedData.bh_hasGear(GearSlot.MEDKIT) && loadedData.bh_hasGear(GearSlot.STABILIZER),
                ctx + "gear flags should be recomputed to match the reloaded gear container");

        helper.assertTrue(loadedData.bh_getChestContainer().getItem(0).is(Items.DIAMOND)
                        && loadedData.bh_getChestContainer().getItem(0).getCount() == 3,
                ctx + "chest slot 0 should survive");
        helper.assertTrue(loadedData.bh_getChestContainer().getItem(26).is(Items.GOLD_INGOT),
                ctx + "chest last slot should survive");
        helper.assertTrue(loadedData.bh_hasUpgradedSaddle(), ctx + "upgraded saddle should survive (vanilla's own SaddleItem save)");

        helper.assertTrue(loadedData.bh_hasCartChest(), ctx + "cart chest flag should survive");
        helper.assertTrue(loadedData.bh_getCartChestContainer().getItem(0).is(Items.IRON_INGOT),
                ctx + "cart chest slot 0 should survive");
        helper.assertTrue(loadedData.bh_getCartChestContainer().getItem(52).is(Items.EMERALD),
                ctx + "cart chest last slot should survive");
        helper.assertTrue(loadedData.bh_hasCartPlough() && loadedData.bh_getCartPlough().is(Items.IRON_HOE),
                ctx + "cart plough should survive");
        boolean expectLarge = breed.archetype() == icy.betterhorses.net.BreedArchetype.DRAFT;
        helper.assertTrue(loadedData.bh_hasLargeCart() == expectLarge,
                ctx + "large cart flag should survive (draft breeds only)");
    }

    // Fields that reset to their construction defaults on reload - confirmed intentional: they are
    // all per-session/combat/ability timers or gait state that AbstractHorseMixin.bh_onWrite simply
    // never writes (there is no BH_ key for any of them). Not a bug list, just documentation of the
    // boundary the round-trip test above deliberately doesn't assert on.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void transientFieldsResetOnReload(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, net.minecraft.world.level.block.Blocks.STONE);
        AbstractHorse original = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(original);
        data.bh_setBreed(HorseBreed.CLYDESDALE);
        data.bh_setStabilizerState(icy.betterhorses.net.HorseStabilizerState.OPEN);
        data.bh_setGaitGear(2);
        data.bh_setFreeLook(true);
        data.bh_setStompTicks(5);
        data.bh_setCombatTarget(UUID.randomUUID());
        data.bh_setSpookTicks(30);
        data.bh_setKickTicks(4);
        data.bh_setSurge(7);
        data.bh_setPerkSurge(9);
        data.bh_setPulse(11);
        data.bh_setCharge(13);
        data.bh_setCartId(UUID.randomUUID());

        ServerLevel level = helper.getLevel();
        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        AbstractHorse loaded = ModEntities.CLYDESDALE_HORSE.create(level);
        helper.assertTrue(loaded != null, "failed to create a fresh Clydesdale for load");
        loaded.load(saved);
        IHorseData loadedData = IHorseData.of(loaded);

        helper.assertTrue(loadedData.bh_getStabilizerState() == icy.betterhorses.net.HorseStabilizerState.CLOSED,
                "stabilizer state is not persisted - resets to CLOSED (intentional, no BH_ key written for it)");
        helper.assertTrue(loadedData.bh_getGaitGear() == 0, "gait gear is not persisted (intentional)");
        helper.assertFalse(loadedData.bh_isFreeLook(), "free-look is not persisted (intentional, client display only)");
        helper.assertTrue(loadedData.bh_getCombatTarget() == null, "combat target is not persisted (intentional)");
        helper.assertTrue(loadedData.bh_getCartId() == null, "cart id is not persisted directly (rebuilt from the cart entity)");
        helper.succeed();
    }

    // Round 2, item 7: bh_setHome(BlockPos) now also records bh_homeDim (the dimension the home was
    // set in), so HorseManagement.sendHome's cross-dimension guard can actually fire for a loaded
    // horse. Was homeDimensionIsNeverRecorded_KNOWN_BUG.
    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void homeDimensionIsRecordedWhenHomeIsSet(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, net.minecraft.world.level.block.Blocks.STONE);
        AbstractHorse horse = helper.spawn(ModEntities.CLYDESDALE_HORSE, 2, 2, 2);
        IHorseData data = IHorseData.of(horse);
        data.bh_setBreed(HorseBreed.CLYDESDALE);

        data.bh_setHome(horse.blockPosition());

        helper.assertTrue(data.bh_getHomeDimension() != null,
                "bh_getHomeDimension() should record the dimension bh_setHome was called in, so "
                        + "HorseManagement.sendHome's cross-dimension check can fire for a loaded horse");
        helper.succeed();
    }
}
