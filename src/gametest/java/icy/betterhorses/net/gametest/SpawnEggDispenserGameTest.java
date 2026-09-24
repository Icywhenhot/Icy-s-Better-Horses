package icy.betterhorses.net.gametest;

import icy.betterhorses.net.ModEntities;
import icy.betterhorses.net.ModItems;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

// Round 2, item 11: vanilla registers its spawn-egg dispense behaviour in DispenserBlock's static
// init, which runs before our SpawnEggItem instances exist, so dispensers used to no-op on our eggs.
// This drives the exact behaviour DispenserBlock.registerBehavior stored for a breed egg, rather
// than fighting redstone/world-placement timing to trigger a real dispenser block.
public class SpawnEggDispenserGameTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE, timeoutTicks = 20)
    public void dispenserBehaviourIsRegisteredForBreedEggsAndSpawnsTheRightHorse(GameTestHelper helper) {
        helper.setBlock(2, 1, 2, Blocks.STONE);
        BlockState dispenserState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP);
        helper.setBlock(2, 2, 2, dispenserState);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        ServerLevel level = helper.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        DispenseItemBehavior behavior = registeredBehavior(ModItems.CLYDESDALE_HORSE_SPAWN_EGG);
        helper.assertTrue(behavior != null, "a dispense behaviour should be registered for the Clydesdale spawn egg");

        BlockSource blockSource = new BlockSource() {
            @Override public double x() { return pos.getX() + 0.5; }
            @Override public double y() { return pos.getY() + 0.5; }
            @Override public double z() { return pos.getZ() + 0.5; }
            @Override public BlockPos getPos() { return pos; }
            @Override public BlockState getBlockState() { return dispenserState; }
            @Override public <T extends BlockEntity> T getEntity() { return (T) blockEntity; }
            @Override public ServerLevel getLevel() { return level; }
        };

        behavior.dispense(blockSource, new ItemStack(ModItems.CLYDESDALE_HORSE_SPAWN_EGG));

        AABB nearby = new AABB(pos).inflate(3);
        List<AbstractHorse> spawned = level.getEntitiesOfClass(AbstractHorse.class, nearby,
                h -> h.getType() == ModEntities.CLYDESDALE_HORSE);
        helper.assertTrue(!spawned.isEmpty(), "the registered behaviour should have spawned a Clydesdale");
        helper.succeed();
    }

    @SuppressWarnings("unchecked")
    private static DispenseItemBehavior registeredBehavior(net.minecraft.world.item.Item item) {
        try {
            Field field = DispenserBlock.class.getDeclaredField("DISPENSER_REGISTRY");
            field.setAccessible(true);
            Map<net.minecraft.world.item.Item, DispenseItemBehavior> registry =
                    (Map<net.minecraft.world.item.Item, DispenseItemBehavior>) field.get(null);
            return registry.get(item);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
