package icy.betterhorses.net.feature;

import icy.betterhorses.net.BhConfig;
import icy.betterhorses.net.IHorseData;
import icy.betterhorses.net.inventory.GearSlot;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class FrostHooves implements HorseFeature {

    private static final double SAMPLE_STEP = 0.75D;
    private static final double RESET_DISTANCE = 8.0D;

    private @Nullable Vec3 lastPos;

    @Override
    public void tick(AbstractHorse horse, IHorseData data) {
        Vec3 currentPos = horse.position();
        Vec3 previousPos = this.lastPos;
        this.lastPos = currentPos;

        if (!(horse.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int frostWalkerLevel = frostWalkerLevel(data);
        if (frostWalkerLevel <= 0 || horse.isInLava() || (!horse.onGround() && !horse.isInWater())) {
            return;
        }

        if (previousPos == null
                || previousPos.distanceToSqr(currentPos) > RESET_DISTANCE * RESET_DISTANCE) {
            previousPos = currentPos;
        }

        applyTrail(serverLevel, previousPos, currentPos, frostWalkerLevel);
    }

    private static int frostWalkerLevel(IHorseData data) {
        if (!BhConfig.hoovesEnabled()) {
            return 0;
        }
        ItemStack hooves = data.bh_getGearContainer().getItem(GearSlot.HOOVES.ordinal());
        if (hooves.isEmpty()) {
            return 0;
        }

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : hooves.getEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.FROST_WALKER)) {
                return entry.getIntValue();
            }
        }

        return 0;
    }

    private static void applyTrail(ServerLevel level, Vec3 start, Vec3 end, int frostWalkerLevel) {
        int radius = Math.min(16, 3 + frostWalkerLevel);
        double dx = end.x - start.x;
        double dz = end.z - start.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < 1.0E-6D) {
            freezeAtSample(level, end, radius);
            return;
        }
        int samples = Math.max(1, Mth.ceil(horizontalDistance / SAMPLE_STEP));

        for (int i = 0; i <= samples; i++) {
            double progress = (double) i / (double) samples;
            freezeAtSample(level, new Vec3(
                    Mth.lerp(progress, start.x, end.x),
                    Mth.lerp(progress, start.y, end.y),
                    Mth.lerp(progress, start.z, end.z)),
                    radius);
        }
    }

    private static void freezeAtSample(ServerLevel level, Vec3 sample, int radius) {
        BlockPos center = BlockPos.containing(sample.x, sample.y - 1.0D, sample.z);
        BlockState frostedIce = Blocks.FROSTED_ICE.defaultBlockState();
        int radiusSq = radius * radius;
        BlockPos.MutableBlockPos waterPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos abovePos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radiusSq) {
                    continue;
                }

                waterPos.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                BlockState waterState = level.getBlockState(waterPos);
                if (!waterState.is(Blocks.WATER) || !level.getFluidState(waterPos).isSourceOfType(Fluids.WATER)) {
                    continue;
                }

                abovePos.set(waterPos.getX(), waterPos.getY() + 1, waterPos.getZ());
                if (!level.getBlockState(abovePos).isAir()) {
                    continue;
                }

                level.setBlock(waterPos, frostedIce, 3);
            }
        }
    }
}
