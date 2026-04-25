package icy.betterhorses.net;

import icy.betterhorses.net.item.HitchpostBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class HitchpostPlacementTracker {

    private static final Map<UUID, PendingPlacement> PENDING_PLACEMENTS = new HashMap<>();

    public static void setPendingPlacement(ServerPlayer player, BlockPos pos, int slot) {
        clearPendingPlacement(player);
        PENDING_PLACEMENTS.put(player.getUUID(), new PendingPlacement(player.level().dimension(), pos.immutable(), slot));
    }

    public static boolean confirmPendingPlacement(
            ServerPlayer player,
            AbstractHorse horse,
            ItemStack stack,
            InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !stack.is(ModItems.HITCHPOST)) {
            clearPendingPlacement(player);
            return false;
        }

        PendingPlacement pending = PENDING_PLACEMENTS.get(player.getUUID());
        if (pending == null) {
            return false;
        }

        if (player.getInventory().selected != pending.slot()) {
            clearPendingPlacement(player);
            return false;
        }

        ServerLevel level = player.server.getLevel(pending.dimension());
        if (level == null || level != horse.level()) {
            clearPendingPlacement(player);
            return false;
        }

        BlockState state = level.getBlockState(pending.pos());
        if (!state.is(ModBlocks.HITCHPOST)) {
            PENDING_PLACEMENTS.remove(player.getUUID());
            return false;
        }

        if (state.hasProperty(HitchpostBlock.CONFIRMED) && !state.getValue(HitchpostBlock.CONFIRMED)) {
            level.setBlock(pending.pos(), state.setValue(HitchpostBlock.CONFIRMED, true), Block.UPDATE_ALL);
        }

        LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(level, pending.pos());
        knot.playPlacementSound();
        horse.setLeashedTo(knot, true);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        PENDING_PLACEMENTS.remove(player.getUUID());
        return true;
    }

    public static void clearPendingPlacement(ServerPlayer player) {
        PendingPlacement pending = PENDING_PLACEMENTS.remove(player.getUUID());
        if (pending != null) {
            removePendingBlock(player.server, pending);
        }
    }

    public static void clearPendingPlacementAt(Level level, BlockPos pos) {
        Iterator<Map.Entry<UUID, PendingPlacement>> iterator = PENDING_PLACEMENTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingPlacement> entry = iterator.next();
            PendingPlacement pending = entry.getValue();
            if (pending.dimension().equals(level.dimension()) && pending.pos().equals(pos)) {
                iterator.remove();
            }
        }
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, PendingPlacement>> iterator = PENDING_PLACEMENTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingPlacement> entry = iterator.next();
            UUID playerId = entry.getKey();
            PendingPlacement pending = entry.getValue();

            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                removePendingBlock(server, pending);
                iterator.remove();
                continue;
            }

            ServerLevel level = server.getLevel(pending.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }

            BlockState state = level.getBlockState(pending.pos());
            if (!state.is(ModBlocks.HITCHPOST)) {
                iterator.remove();
                continue;
            }

            boolean stillPending = !state.hasProperty(HitchpostBlock.CONFIRMED)
                    || !state.getValue(HitchpostBlock.CONFIRMED);
            boolean stillHoldingItem = player.level() == level
                    && player.getInventory().selected == pending.slot()
                    && player.getMainHandItem().is(ModItems.HITCHPOST);

            if (stillPending && stillHoldingItem) {
                continue;
            }

            if (stillPending) {
                removePendingBlock(server, pending);
            }
            iterator.remove();
        }
    }

    private static void removePendingBlock(MinecraftServer server, PendingPlacement pending) {
        ServerLevel level = server.getLevel(pending.dimension());
        if (level == null) {
            return;
        }

        BlockState state = level.getBlockState(pending.pos());
        if (!state.is(ModBlocks.HITCHPOST)) {
            return;
        }

        if (!state.hasProperty(HitchpostBlock.CONFIRMED) || state.getValue(HitchpostBlock.CONFIRMED)) {
            return;
        }

        HitchpostBlock.releaseLeashedMobs(level, pending.pos());
        level.removeBlock(pending.pos(), false);
    }

    private record PendingPlacement(ResourceKey<Level> dimension, BlockPos pos, int slot) {}

    private HitchpostPlacementTracker() {}
}
