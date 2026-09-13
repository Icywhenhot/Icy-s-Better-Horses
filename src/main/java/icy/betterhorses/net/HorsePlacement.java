package icy.betterhorses.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class HorsePlacement {
    private HorsePlacement() {}

    public static @Nullable Vec3 find(AbstractHorse horse, BlockPos target) {
        ServerLevel level = (ServerLevel) horse.level();
        for (int radius = 0; radius <= 4; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.max(Math.abs(x), Math.abs(z)) != radius) continue;
                    for (int y : new int[]{0, 1, -1, 2, -2}) {
                        BlockPos pos = target.offset(x, y, z);
                        if (!level.hasChunkAt(pos) || !level.isInWorldBounds(pos)) continue;
                        Vec3 at = Vec3.atBottomCenterOf(pos);
                        AABB box = horse.getBoundingBox().move(at.subtract(horse.position()));
                        if (!level.getWorldBorder().isWithinBounds(box)
                                || !level.noCollision(horse, box) || level.containsAnyLiquid(box)) continue;
                        boolean supported = true;
                        for (BlockPos inside : BlockPos.betweenClosed(
                                BlockPos.containing(box.minX, box.minY, box.minZ),
                                BlockPos.containing(box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D))) {
                            var state = level.getBlockState(inside);
                            if (!level.hasChunkAt(inside) || !level.isInWorldBounds(inside)
                                    || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                                    || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE)
                                    || state.is(Blocks.POWDER_SNOW)) {
                                supported = false;
                                break;
                            }
                        }
                        if (!supported) continue;
                        for (BlockPos floor : BlockPos.betweenClosed(
                                BlockPos.containing(box.minX + 0.05D, box.minY - 0.01D, box.minZ + 0.05D),
                                BlockPos.containing(box.maxX - 0.05D, box.minY - 0.01D, box.maxZ - 0.05D))) {
                            var state = level.getBlockState(floor);
                            if (!state.isFaceSturdy(level, floor, Direction.UP)
                                    || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
                                    || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                                supported = false;
                                break;
                            }
                        }
                        if (supported) return at;
                    }
                }
            }
        }
        return null;
    }

    public static boolean teleport(AbstractHorse horse, BlockPos target) {
        if (IHorseData.of(horse).bh_hasCartGear()) return false;
        Vec3 at = find(horse, target);
        if (at == null) return false;
        horse.ejectPassengers();
        horse.teleportTo(at.x, at.y, at.z);
        horse.fallDistance = 0.0F;
        horse.setDeltaMovement(Vec3.ZERO);
        return true;
    }
}
