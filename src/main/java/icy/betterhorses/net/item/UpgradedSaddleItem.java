package icy.betterhorses.net.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// 1.21.5+ replaced isSaddled/isSaddleable/equipSaddle with the generic equipment-slot system: saddles live in EquipmentSlot.SADDLE, read/set via getItemBySlot/setItemSlot, and canUseSlot(SADDLE) replaces the old isSaddleable gate.
public class UpgradedSaddleItem extends Item {
    public UpgradedSaddleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AbstractHorse horse)) return InteractionResult.PASS;
        if (!horse.isTamed() || horse.isBaby() || !horse.canUseSlot(EquipmentSlot.SADDLE)) {
            return InteractionResult.PASS;
        }
        if (!horse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide()) {
            horse.setItemSlot(EquipmentSlot.SADDLE, stack.copyWithCount(1));
            horse.level().playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                    SoundEvents.HORSE_SADDLE, SoundSource.NEUTRAL, 0.5F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return player.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
