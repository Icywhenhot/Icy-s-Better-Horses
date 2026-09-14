package icy.betterhorses.net.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class HearthlightBlock extends Block {
    public static final MapCodec<HearthlightBlock> CODEC = simpleCodec(HearthlightBlock::new);
    private static final Map<ServerLevel, Map<BlockPos, Map<UUID, Long>>> lights = new WeakHashMap<>();

    public HearthlightBlock(Properties properties) { super(properties); }

    @Override
    public MapCodec<HearthlightBlock> codec() { return CODEC; }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
            net.minecraft.world.level.BlockGetter level, BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    public void hold(ServerLevel level, BlockPos pos, UUID owner) {
        lights.computeIfAbsent(level, key -> new HashMap<>())
                .computeIfAbsent(pos.immutable(), key -> new HashMap<>()).put(owner, level.getGameTime() + 40L);
        if (!level.getBlockState(pos).is(this)) level.setBlockAndUpdate(pos, defaultBlockState());
        level.scheduleTick(pos, this, 20);
    }

    public void release(ServerLevel level, BlockPos pos, UUID owner) {
        var world = lights.get(level);
        var owners = world == null ? null : world.get(pos);
        if (owners != null) {
            owners.remove(owner);
            if (!owners.isEmpty()) return;
            world.remove(pos);
        }
        if (level.getBlockState(pos).is(this)) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var world = lights.get(level);
        var owners = world == null ? null : world.get(pos);
        if (owners != null) owners.values().removeIf(until -> until <= level.getGameTime());
        if (owners == null || owners.isEmpty()) {
            if (world != null) world.remove(pos);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        } else {
            level.scheduleTick(pos, this, 20);
        }
    }
}
