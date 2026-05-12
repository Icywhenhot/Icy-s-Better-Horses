package icy.betterhorses.net.item;

import com.mojang.serialization.MapCodec;
import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.IcysBetterHorses;
import icy.betterhorses.net.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * Standalone hitch post block that keeps its own tethered-horse state instead of delegating to
 * vanilla fence/lead mechanics.
 */
public class HitchpostBlock extends BaseEntityBlock {

    public static final MapCodec<HitchpostBlock> CODEC = simpleCodec(HitchpostBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final double TETHER_SEARCH_RADIUS = 7.0D;
    private static final double TETHER_OFFSET = 1.35D;
    private static final VoxelShape X_AXIS_SHAPE = Shapes.or(
            Block.box(6.0D, 0.0D, 7.0D, 10.0D, 13.0D, 9.0D),
            Block.box(0.0D, 13.0D, 6.0D, 16.0D, 15.0D, 10.0D));
    private static final VoxelShape Z_AXIS_SHAPE = Shapes.or(
            Block.box(7.0D, 0.0D, 6.0D, 9.0D, 13.0D, 10.0D),
            Block.box(6.0D, 13.0D, 0.0D, 10.0D, 15.0D, 16.0D));

    public HitchpostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HitchpostBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getBaseShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getBaseShape(state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!BhConfig.hitchpostEnabled()) {
            return;
        }
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) || !(placer instanceof Player player)) {
            return;
        }

        AbstractHorse horse = findHorseToTether(serverLevel, pos, player);
        if (horse == null) {
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.no_horse_to_tether"));
            return;
        }

        if (tetherHorse(serverLevel, pos, state, horse, player)) {
            player.sendSystemMessage(Component.translatable("message.icys-better-horses.hitchpost_tethered"));
        }
    }

    /**
     * 1.21.5+ replaces {@code onRemove} with {@link #affectNeighborsAfterRemoval}, which is only
     * invoked when the block is actually removed (not for in-place state changes), so we no longer
     * need the {@code !state.is(newState.getBlock())} guard.
     */
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        releaseHorseAtPost(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    public static boolean isValidTether(ServerLevel level, AbstractHorse horse, BlockPos pos) {
        if (!BhConfig.hitchpostEnabled()) {
            return false;
        }
        if (!level.getBlockState(pos).is(ModBlocks.HITCHPOST)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HitchpostBlockEntity hitchpost)) {
            return false;
        }

        return Objects.equals(hitchpost.getTetheredHorseId(), horse.getUUID());
    }

    public static void releaseHorse(ServerLevel level, AbstractHorse horse, boolean logRelease) {
        IHorseData data = (IHorseData) horse;
        BlockPos hitchpostPos = data.bh_getHitchpostPos();
        if (hitchpostPos != null) {
            clearPostReference(level, hitchpostPos, horse.getUUID());
        }

        data.bh_setHitchpostPos(null);
        if (logRelease && hitchpostPos != null) {
            IcysBetterHorses.LOGGER.info("Horse {} released from hitch post at {}", horse.getUUID(), hitchpostPos);
        }
    }

    public static void releaseHorseAtPost(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HitchpostBlockEntity hitchpost)) {
            return;
        }

        UUID horseId = hitchpost.getTetheredHorseId();
        if (horseId == null) {
            return;
        }

        if (level.getEntity(horseId) instanceof AbstractHorse horse) {
            ((IHorseData) horse).bh_setHitchpostPos(null);
        }

        hitchpost.setTetheredHorseId(null);
        IcysBetterHorses.LOGGER.info("Horse {} released from hitch post at {}", horseId, pos);
    }

    private static boolean tetherHorse(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            AbstractHorse horse,
            @Nullable Player player) {
        if (!BhConfig.hitchpostEnabled()) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HitchpostBlockEntity hitchpost)) {
            return false;
        }

        UUID horseId = horse.getUUID();
        UUID occupant = hitchpost.getTetheredHorseId();
        if (occupant != null && !occupant.equals(horseId)) {
            return false;
        }

        IHorseData data = (IHorseData) horse;
        BlockPos existingPost = data.bh_getHitchpostPos();
        if (existingPost != null && !existingPost.equals(pos)) {
            clearPostReference(level, existingPost, horseId);
        }

        Vec3 anchor = chooseAnchor(pos, state, horse);
        horse.getNavigation().stop();
        horse.teleportTo(anchor.x, horse.getY(), anchor.z);
        horse.setDeltaMovement(Vec3.ZERO);
        horse.hurtMarked = true;

        if (player != null && data.bh_getOwner() == null) {
            net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> ownerRef = horse.getOwnerReference();
            UUID horseOwner = ownerRef == null ? null : ownerRef.getUUID();
            if (player.getUUID().equals(horseOwner)) {
                data.bh_setOwner(player.getUUID());
            }
        }

        data.bh_setHitchpostPos(pos);
        hitchpost.setTetheredHorseId(horseId);
        IcysBetterHorses.LOGGER.info("Horse {} tethered to hitch post at {}", horseId, pos);
        return true;
    }

    private static @Nullable AbstractHorse findHorseToTether(ServerLevel level, BlockPos pos, Player player) {
        if (player.getVehicle() instanceof AbstractHorse riddenHorse && canTetherHorse(player, riddenHorse)) {
            return riddenHorse;
        }

        AABB searchBox = new AABB(pos).inflate(TETHER_SEARCH_RADIUS, 3.0D, TETHER_SEARCH_RADIUS);
        return level.getEntitiesOfClass(AbstractHorse.class, searchBox, horse -> canTetherHorse(player, horse))
                .stream()
                .min(Comparator.comparingDouble(horse -> horse.distanceToSqr(
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D)))
                .orElse(null);
    }

    private static boolean canTetherHorse(Player player, AbstractHorse horse) {
        if (!horse.isAlive()) {
            return false;
        }

        UUID playerId = player.getUUID();
        net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> ownerRef = horse.getOwnerReference();
        UUID ownerId = ownerRef == null ? null : ownerRef.getUUID();
        UUID modOwnerId = ((IHorseData) horse).bh_getOwner();
        return playerId.equals(ownerId) || playerId.equals(modOwnerId);
    }

    private static Vec3 chooseAnchor(BlockPos pos, BlockState state, AbstractHorse horse) {
        Vec3 center = Vec3.atCenterOf(pos);
        double dx = horse.getX() - center.x;
        double dz = horse.getZ() - center.z;

        Direction direction;
        if (Math.abs(dx) >= Math.abs(dz) && Math.abs(dx) > 0.2D) {
            direction = dx >= 0.0D ? Direction.EAST : Direction.WEST;
        } else if (Math.abs(dz) > 0.2D) {
            direction = dz >= 0.0D ? Direction.SOUTH : Direction.NORTH;
        } else {
            direction = state.getValue(FACING);
        }

        return new Vec3(
                center.x + direction.getStepX() * TETHER_OFFSET,
                horse.getY(),
                center.z + direction.getStepZ() * TETHER_OFFSET);
    }

    private static void clearPostReference(ServerLevel level, BlockPos pos, UUID horseId) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof HitchpostBlockEntity hitchpost)) {
            return;
        }

        if (horseId.equals(hitchpost.getTetheredHorseId())) {
            hitchpost.setTetheredHorseId(null);
        }
    }

    private VoxelShape getBaseShape(BlockState state) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> Z_AXIS_SHAPE;
            case Z -> X_AXIS_SHAPE;
            default -> X_AXIS_SHAPE;
        };
    }
}
