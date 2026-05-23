package icy.betterhorses.net.item;

import icy.betterhorses.net.IHorseData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradedSaddleItem extends Item {
    public UpgradedSaddleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AbstractHorse horse)) return InteractionResult.PASS;
        if (!horse.isTamed() || horse.isBaby() || horse.isSaddled() || !horse.isSaddleable()) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            ((IHorseData) horse).bh_equipUpgradedSaddle(stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }
}
