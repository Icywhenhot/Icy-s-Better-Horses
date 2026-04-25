package icy.betterhorses.net.item;

import icy.betterhorses.net.HitchpostPlacementTracker;
import icy.betterhorses.net.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockItem for the Hitchpost. Right-clicking a block creates a temporary hitchpost without
 * consuming the item. Right-clicking a horse next confirms the post, leashes the horse to it,
 * and only then consumes one hitchpost item.
 */
public class HitchpostBlockItem extends BlockItem {

    public HitchpostBlockItem(Properties properties) {
        super(ModBlocks.HITCHPOST, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        HitchpostPlacementTracker.clearPendingPlacement(serverPlayer);

        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockState placementState = this.getPlacementState(placeContext);
        if (placementState == null || !this.canPlace(placeContext, placementState)) {
            return InteractionResult.FAIL;
        }

        BlockPos placePos = placeContext.getClickedPos();
        BlockState pendingState = placementState.hasProperty(HitchpostBlock.CONFIRMED)
                ? placementState.setValue(HitchpostBlock.CONFIRMED, false)
                : placementState;

        Level level = context.getLevel();
        if (!level.setBlock(placePos, pendingState, Block.UPDATE_ALL)) {
            return InteractionResult.FAIL;
        }

        var soundType = pendingState.getSoundType();
        level.playSound(
                null,
                placePos,
                soundType.getPlaceSound(),
                SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F);

        HitchpostPlacementTracker.setPendingPlacement(serverPlayer, placePos, serverPlayer.getInventory().selected);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return target instanceof AbstractHorse && hand == InteractionHand.MAIN_HAND
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        if (!(target instanceof AbstractHorse horse)) {
            HitchpostPlacementTracker.clearPendingPlacement(serverPlayer);
            return InteractionResult.PASS;
        }
        if (hand != InteractionHand.MAIN_HAND || player.isSecondaryUseActive()) {
            HitchpostPlacementTracker.clearPendingPlacement(serverPlayer);
            return InteractionResult.PASS;
        }

        Level level = horse.level();
        if (!HitchpostPlacementTracker.confirmPendingPlacement(serverPlayer, horse, stack, hand)) {
            player.displayClientMessage(
                    Component.translatable("message.icys-better-horses.no_hitchpost"), true);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
